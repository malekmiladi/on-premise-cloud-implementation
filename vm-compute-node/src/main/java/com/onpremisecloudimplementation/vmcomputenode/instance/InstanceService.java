package com.onpremisecloudimplementation.vmcomputenode.instance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onpremisecloud.grpcservices.ComputeNodeError;
import com.onpremisecloud.grpcservices.InstanceSpecifications;
import com.onpremisecloud.grpcservices.ComputeNodeResponse;
import com.onpremisecloud.grpcservices.UpdatedInstanceMetadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInterface;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.Stream;
import org.libvirt.DomainInterface.InterfaceAddress;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:instance.properties")
public class InstanceService {

    private Connect hypervisorConnection;
    private StoragePool defaultStoragePool;
    @Value("${on-premise-cloud.cloud-data-source.api}")
    private String cloudDataSourceBaseUrl;

    @PostConstruct
    private void initializeEnvironment() {

        try {

            hypervisorConnection = new Connect("qemu:///system");
            createDefaultStoragePoolIfNotExists(hypervisorConnection);
            defaultStoragePool = hypervisorConnection.storagePoolLookupByName("default");

        } catch (LibvirtException exception) {

            /**
             * TODO: Kill app here
             * TODO: Better exception handling
             */
            log.error("Application environment initialization failed: {}\nExiting.", exception.getMessage());

        }

    }

    private void createDefaultStoragePoolIfNotExists(Connect hypervisorConnection) {

        try {

            hypervisorConnection.storagePoolLookupByName("default");
            
        } catch (LibvirtException exception) {
            
            createDefaultStoragePool(hypervisorConnection);

        }

    }

    private void createDefaultStoragePool(Connect hypervisorConnection) {

        String defaultStoragePoolXML = """
                <pool type='dir'>
                    <name>default</name>
                    <target>
                        <path>/var/lib/libvirt/images</path>
                    </target>
                </pool>
                """;

        try {

            StoragePool _defaultStoragePool = hypervisorConnection.storagePoolDefineXML(defaultStoragePoolXML, 0);
            _defaultStoragePool.setAutostart(1);
            _defaultStoragePool.create(0);
        
        } catch (LibvirtException exception) {

            /**
             * TODO: Kill app here
             */
            log.error("Error creating storage pool{}\nStopping application.", exception.getMessage());

        }

    }

    public ComputeNodeResponse createVirtualMachine(InstanceSpecifications newInstanceRequest) {

        String instanceId = newInstanceRequest.getInstanceId();
        String image = newInstanceRequest.getImage().getDownloadUrl();
        int vram = newInstanceRequest.getMemorySizeGb();
        int vcpus = newInstanceRequest.getCpusCount();
        int size = newInstanceRequest.getDiskSizeGb();
        log.info("Creating new instance with specs: instance.memory={} instance.cpus={} instance.storage={}", vram, vcpus, size);
        // we assume that everything is ok until an exception occurs
        ComputeNodeError.Builder error = ComputeNodeError.newBuilder()
                                            .setType(0)
                                            .setMessage("");
        UpdatedInstanceMetadata.Builder metadata = UpdatedInstanceMetadata.newBuilder()
                                            .setExternalIp("")
                                            .setInternalIp("")
                                            .setState("");
        ComputeNodeResponse.Builder response = ComputeNodeResponse.newBuilder();
        try {

            /*
             * TODO: Find better way of exploiting cloud init's datasource (maybe
             * create my own datasource and discovery strategies)
            */
            String virtualMachineXMLConfig = generateVmXMLConfig(
                instanceId,
                "",
                vram,
                vcpus,
                    cloudDataSourceBaseUrl + newInstanceRequest.getInstanceName() + "/"
            );

            StorageVol instanceVolume = createVolume(size, instanceId, image);
            downloadAndUploadImageToVolume(instanceVolume, "");
            Domain virtualMachine = hypervisorConnection.domainDefineXML(virtualMachineXMLConfig);
            virtualMachine.setAutostart(true);
            virtualMachine.create();
            log.info("Started virtual machine instance: instance.name={} instance.uuid={}", virtualMachine.getName(), virtualMachine.getID());
            String virtualMachineIpv4 = "";
            Collection<DomainInterface> interfaces = new ArrayList<>();
            while (interfaces.isEmpty()) {
                interfaces = virtualMachine.interfaceAddresses(Domain.InterfaceAddressesSource.VIR_DOMAIN_INTERFACE_ADDRESSES_SRC_LEASE, 0);
            }
            
            for (DomainInterface iface : interfaces) {
                Collection<InterfaceAddress> addrs = iface.addrs;
                for (InterfaceAddress addr : addrs) {
                    InetAddress interfaceAddress = addr.address;
                    virtualMachineIpv4 = interfaceAddress.getHostAddress();
                }
            }

            metadata.setExternalIp("")
                    .setInternalIp(virtualMachineIpv4)
                    .setState("running");

        } catch (LibvirtException exception) {

            error.setType(1)
                .setMessage(exception.getMessage());
            log.error("Failed to create instance: {}", exception.getMessage());

        } catch (ParserConfigurationException exception) {

            error.setType(2)
                .setMessage(exception.getMessage());
            log.error("Failed to create VM config XML description: {}", exception.getMessage());

        } catch (TransformerConfigurationException exception) {

            error.setType(3)
                .setMessage(exception.getMessage());
            log.error("Failed to create XML transformer: {}", exception.getMessage());

        } catch (TransformerException exception) {

            error.setType(4)
                .setMessage(exception.getMessage());
            log.error("Failed to convert XML object to text: {}", exception.getMessage());

        } catch (FileNotFoundException exception) {

            //TODO: Replace with download error
            error.setType(5)
                .setMessage(exception.getMessage());
            log.error("Image not found: {}", exception.getMessage());

        } catch (IOException exception) {

            error.setType(6)
                .setMessage(exception.getMessage());
            log.error("Error uploading image to volume: {}", exception.getMessage());

        } finally {
            response.setAck(error.getType() == 0);
            response.setMetadata(metadata);
            response.setError(error);
        }

        return response.build();

    }

    private StorageVol createVolume(int size, String instanceId, String image)
    throws LibvirtException, ParserConfigurationException, TransformerException {

        String volumeXMLConfig = generateVolXMLConfig(instanceId, size);
        return defaultStoragePool.storageVolCreateXML(volumeXMLConfig, 0);

    }

    private void downloadAndUploadImageToVolume(StorageVol instanceVolume, String image)
    throws LibvirtException, IOException {

        /**
         * TODO: Download OS in chunks and upload them to volume instead of writing to file.
         * TODO: Add OS selection instead of hardcoding ubuntu
         */

        Stream fileStream = hypervisorConnection.streamNew(0);
        File imageFile = new File("/opt/distributions/stable/jammy-server-cloudimg-amd64-disk-kvm.img");            
        instanceVolume.upload(fileStream, 0, imageFile.length(), 0);
        
        FileInputStream inputStream = new FileInputStream(imageFile);
        byte[] buffer = new byte[4096];
        while (inputStream.read(buffer) != -1) {
            fileStream.send(buffer);
        }
        fileStream.finish();
        inputStream.close();

    }

    private String generateVolXMLConfig(String instanceId, int size)
    throws ParserConfigurationException, TransformerException {
        DocumentBuilder volumeXMLConfigBuilder = DocumentBuilderFactory
                                                    .newInstance()
                                                    .newDocumentBuilder();
                                                    
        Document volumeXMLConfig = volumeXMLConfigBuilder.newDocument();
                                                    
        Element volumeConfigRoot = volumeXMLConfig.createElement("volume");
        volumeConfigRoot.setAttribute("type", "file");
        volumeXMLConfig.appendChild(volumeConfigRoot);
        
        Element volumeName = volumeXMLConfig.createElement("name");
        volumeName.setTextContent(instanceId + ".qcow2");

        volumeConfigRoot.appendChild(volumeName);
        
        Element volumeCapacity = volumeXMLConfig.createElement("capacity");
        volumeCapacity.setAttribute("unit", "G");
        volumeCapacity.setTextContent(String.valueOf(size));

        volumeConfigRoot.appendChild(volumeCapacity);

        Element volumeAllocation = volumeXMLConfig.createElement("allocation");
        volumeAllocation.setAttribute("unit", "G");
        volumeAllocation.setTextContent(String.valueOf(size));
        
        volumeConfigRoot.appendChild(volumeAllocation);

        Element volumeTarget = volumeXMLConfig.createElement("target");
    
        Element volumeFormat = volumeXMLConfig.createElement("format");
        volumeFormat.setAttribute("type", "qcow2");

        volumeTarget.appendChild(volumeFormat);

        volumeConfigRoot.appendChild(volumeTarget);

        TransformerFactory transformXMLFactory = TransformerFactory.newInstance();
        transformXMLFactory.setAttribute("indent-number", 4);
        Transformer xmlTransformer = transformXMLFactory.newTransformer();
        xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter xmlWriter = new StringWriter();
        DOMSource xmlSource = new DOMSource(volumeXMLConfig);
        StreamResult xmlText = new StreamResult(xmlWriter);

        xmlTransformer.transform(xmlSource, xmlText);

        log.debug("VM volume XML description: \n" + xmlWriter);

        return xmlWriter.toString();

    }

    private String generateVmXMLConfig(
        String instanceId,
        String description,
        int vram,
        int vcpu,
        String noCloudDataSource
    ) throws ParserConfigurationException, TransformerException {

        DocumentBuilder vmXMLConfigBuilder = DocumentBuilderFactory
                                                    .newInstance()
                                                    .newDocumentBuilder();
                                                    
        Document vmXMLConfig = vmXMLConfigBuilder.newDocument();
                                                    
        Element vmDomain = vmXMLConfig.createElement("domain");
        vmDomain.setAttribute("type", "kvm");
        vmXMLConfig.appendChild(vmDomain);
        
        Element vmName = vmXMLConfig.createElement("name");
        vmName.setTextContent(instanceId);

        vmDomain.appendChild(vmName);
        
        Element vmDescription = vmXMLConfig.createElement("description");
        vmDescription.setTextContent(description);

        vmDomain.appendChild(vmDescription);
        
        Element vmMemory = vmXMLConfig.createElement("memory");
        vmMemory.setAttribute("unit", "G");
        vmMemory.setTextContent(String.valueOf(vram));

        vmDomain.appendChild(vmMemory);

        Element vmCurrentMemory = vmXMLConfig.createElement("currentMemory");
        vmCurrentMemory.setAttribute("unit", "G");
        vmCurrentMemory.setTextContent(String.valueOf(vram));

        vmDomain.appendChild(vmCurrentMemory);
        
        Element vmVcpu = vmXMLConfig.createElement("vcpu");
        vmVcpu.setAttribute("placement", "static");
        vmVcpu.setTextContent(String.valueOf(vcpu));

        vmDomain.appendChild(vmVcpu);

        Element vmSysInfo = vmXMLConfig.createElement("sysinfo");
        vmSysInfo.setAttribute("type", "smbios");
        
        Element vmSysInfoSystem = vmXMLConfig.createElement("system");
        
        Element vmSysInfoSystemSerial = vmXMLConfig.createElement("entry");
        vmSysInfoSystemSerial.setAttribute("name", "serial");
        vmSysInfoSystemSerial.setTextContent("ds=nocloud;s=" + noCloudDataSource);

        vmSysInfoSystem.appendChild(vmSysInfoSystemSerial);

        vmSysInfo.appendChild(vmSysInfoSystem);

        vmDomain.appendChild(vmSysInfo);
        
        Element vmOS = vmXMLConfig.createElement("os");

        Element vmOsType = vmXMLConfig.createElement("type");
        vmOsType.setTextContent("hvm");

        vmOS.appendChild(vmOsType);

        Element vmBoot = vmXMLConfig.createElement("boot");
        vmBoot.setAttribute("dev", "hd");
        
        vmOS.appendChild(vmBoot);
        
        Element vmOsSmbios = vmXMLConfig.createElement("smbios");
        vmOsSmbios.setAttribute("mode", "sysinfo");

        vmOS.appendChild(vmOsSmbios);
        
        vmDomain.appendChild(vmOS);
        
        Element vmCpu = vmXMLConfig.createElement("cpu");
        vmCpu.setAttribute("mode", "host-passthrough");
        
        vmDomain.appendChild(vmCpu);
        
        Element actionOnReboot = vmXMLConfig.createElement("on_reboot");
        actionOnReboot.setTextContent("restart");
        
        vmDomain.appendChild(actionOnReboot);
        
        Element actionOnCrash = vmXMLConfig.createElement("on_crash");
        actionOnCrash.setTextContent("restart");
        
        vmDomain.appendChild(actionOnCrash);
        
        Element vmDevices = vmXMLConfig.createElement("devices");
        
        Element vmEmulator = vmXMLConfig.createElement("emulator");
        vmEmulator.setTextContent("/usr/bin/qemu-system-x86_64");
        
        vmDevices.appendChild(vmEmulator);
        
        Element vmStorage = vmXMLConfig.createElement("disk");
        vmStorage.setAttribute("type", "file");
        vmStorage.setAttribute("device", "disk");
        
        Element vmStorageDriver = vmXMLConfig.createElement("driver");
        vmStorageDriver.setAttribute("name", "qemu");
        vmStorageDriver.setAttribute("type", "qcow2");
        
        vmStorage.appendChild(vmStorageDriver);
        
        Element vmStorageSource = vmXMLConfig.createElement("source");
        vmStorageSource.setAttribute("file", "/var/lib/libvirt/images/" + instanceId + ".qcow2");
        
        vmStorage.appendChild(vmStorageSource);
        
        Element vmStorageTarget = vmXMLConfig.createElement("target");
        vmStorageTarget.setAttribute("dev", "vda");
        vmStorageTarget.setAttribute("bus", "virtio");
        
        vmStorage.appendChild(vmStorageTarget);
        
        vmDevices.appendChild(vmStorage);
        
        Element vmInterface = vmXMLConfig.createElement("interface");
        vmInterface.setAttribute("type", "network");

        Element vmInterfaceSource = vmXMLConfig.createElement("source");
        vmInterfaceSource.setAttribute("network", "default");
        
        vmInterface.appendChild(vmInterfaceSource);
        
        Element vmInterfaceModel = vmXMLConfig.createElement("model");
        vmInterfaceModel.setAttribute("type", "virtio");

        vmInterface.appendChild(vmInterfaceModel);

        vmDevices.appendChild(vmInterface);

        Element vmSerial = vmXMLConfig.createElement("serial");
        vmSerial.setAttribute("type", "pty");
        
        Element vmSerialSource = vmXMLConfig.createElement("source");
        vmSerialSource.setAttribute("path", "/dev/pts/6");

        vmSerial.appendChild(vmSerialSource);

        Element vmSerialTarget = vmXMLConfig.createElement("target");
        vmSerialTarget.setAttribute("type", "isa-serial");
        vmSerialTarget.setAttribute("port", "0");
        
        Element vmSerialTargetModel = vmXMLConfig.createElement("model");
        vmSerialTargetModel.setAttribute("name", "isa-serial");

        vmSerialTarget.appendChild(vmSerialTargetModel);

        vmSerial.appendChild(vmSerialTarget);

        vmDevices.appendChild(vmSerial);
        
        Element vmConsole = vmXMLConfig.createElement("console");
        vmConsole.setAttribute("type", "pty");

        Element vmConsoleTarget = vmXMLConfig.createElement("target");
        vmConsoleTarget.setAttribute("type", "serial");
        
        vmConsole.appendChild(vmConsoleTarget);
        
        Element vmConsoleAddress = vmXMLConfig.createElement("address");
        vmConsoleAddress.setAttribute("type", "virtio-serial");
        vmConsoleAddress.setAttribute("controller", "0");
        vmConsoleAddress.setAttribute("bus", "0");
        vmConsoleAddress.setAttribute("port", "1");
        
        vmConsole.appendChild(vmConsoleAddress);
        
        vmDevices.appendChild(vmConsole);
        
        Element vmMouse = vmXMLConfig.createElement("input");
        vmMouse.setAttribute("type", "mouse");
        vmMouse.setAttribute("bus", "ps2");
        
        vmDevices.appendChild(vmMouse);
        
        Element vmKeyboard = vmXMLConfig.createElement("input");
        vmKeyboard.setAttribute("type", "keyboard");
        vmKeyboard.setAttribute("bus", "ps2");
        
        vmDevices.appendChild(vmKeyboard);

        vmDomain.appendChild(vmDevices);

        TransformerFactory transformXMLFactory = TransformerFactory.newInstance();
        transformXMLFactory.setAttribute("indent-number", 4);
        Transformer xmlTransformer = transformXMLFactory.newTransformer();
        xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter xmlWriter = new StringWriter();
        DOMSource xmlSource = new DOMSource(vmXMLConfig);
        StreamResult xmlText = new StreamResult(xmlWriter);

        xmlTransformer.transform(xmlSource, xmlText);

        log.debug("VM XML description: \n" + xmlWriter);

        return xmlWriter.toString();
        
    }

    public ComputeNodeResponse deleteVirtualMachine(String instanceId) {

        ComputeNodeError.Builder error = ComputeNodeError.newBuilder()
            .setType(0);
        ComputeNodeResponse.Builder deleteResponse = ComputeNodeResponse.newBuilder();

        try {
            Domain virtualMachineInstance = hypervisorConnection.domainLookupByName(instanceId);

            if (virtualMachineInstance.isActive() == 1) {
                virtualMachineInstance.destroy();
            }
            
            virtualMachineInstance.undefine();
            StorageVol instanceVolume = defaultStoragePool.storageVolLookupByName(instanceId + ".qcow2");
            instanceVolume.delete(0);

            deleteResponse.setAck(true);
        } catch (Exception e) {
            error.setType(1);
            error.setMessage(e.getMessage());
            deleteResponse.setAck(false);
        }
        
        deleteResponse.setError(error.build());
        return deleteResponse.build();
    }

    public ComputeNodeResponse shutdownVirtualMachine(String instanceId) {

        ComputeNodeError.Builder error = ComputeNodeError.newBuilder()
            .setType(0);
        ComputeNodeResponse.Builder shutdownResponse = ComputeNodeResponse.newBuilder();
        
        try {
            Domain virtualMachineInstance = hypervisorConnection.domainLookupByName(instanceId);
            virtualMachineInstance.shutdown();

            shutdownResponse.setAck(true);
        } catch (Exception e) {
            error.setType(1);
            error.setMessage(e.getMessage());
            shutdownResponse.setAck(false);
        }

        shutdownResponse.setError(error.build());
        return shutdownResponse.build();
    }

}
