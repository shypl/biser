package org.shypl.biser.csi.server;

import org.shypl.biser.csi.Address;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ServerSettings {
	private final Address address;
	private       byte[]  crossDomainPolicyResponse;
	
	private int    connectionRecoveryTimeout                    = 60 * 3;
	private int    connectionActivityTimeout                    = 60;
	private long   emulateDelayInConnectionDataProcessingMillis = 0;
	private String backdoorPassword                             = "password";
	
	public ServerSettings(Address address) {
		this.address = address;
		setCrossDomainPolicy("<?xml version=\"1.0\"?>\n"
			+ "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">\n"
			+ "<cross-domain-policy>\n"
			+ "<site-control permitted-cross-domain-policies=\"all\" />\n"
			+ "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\"/>\n"
			+ "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>\n"
			+ "</cross-domain-policy>");
	}
	
	public ServerSettings(Address address, String crossDomainPolicy) {
		this.address = address;
		setCrossDomainPolicy(crossDomainPolicy);
	}
	
	public ServerSettings(Address address, Path crossDomainPolicy) throws IOException {
		this.address = address;
		setCrossDomainPolicy(crossDomainPolicy);
	}
	
	public Address getAddress() {
		return address;
	}
	
	public byte[] getCrossDomainPolicyResponse() {
		return crossDomainPolicyResponse;
	}
	
	public void setCrossDomainPolicy(String value) {
		setCrossDomainPolicy(value.getBytes(StandardCharsets.UTF_8));
	}
	
	public void setCrossDomainPolicy(Path path) throws IOException {
		setCrossDomainPolicy(Files.readAllBytes(path));
	}
	
	public void setCrossDomainPolicy(byte[] bytes) {
		if (bytes[bytes.length - 1] == 0) {
			crossDomainPolicyResponse = Arrays.copyOf(bytes, bytes.length);
		}
		else {
			crossDomainPolicyResponse = Arrays.copyOf(bytes, bytes.length + 1);
		}
	}
	
	public int getConnectionRecoveryTimeout() {
		return connectionRecoveryTimeout;
	}
	
	public void setConnectionRecoveryTimeout(int value) {
		connectionRecoveryTimeout = value;
	}
	
	public int getConnectionActivityTimeout() {
		return connectionActivityTimeout;
	}
	
	public void setConnectionActivityTimeout(int value) {
		connectionActivityTimeout = value;
	}
	
	public boolean isEmulateDelayInConnectionDataProcessing() {
		return emulateDelayInConnectionDataProcessingMillis > 0;
	}
	
	public void setEmulateDelayInConnectionDataProcessingMillis(long millis) {
		this.emulateDelayInConnectionDataProcessingMillis = millis;
	}
	
	public long getEmulateDelayInConnectionDataProcessingMillis() {
		return emulateDelayInConnectionDataProcessingMillis;
	}
	
	public String getBackdoorPassword() {
		return backdoorPassword;
	}
	
	public void setBackdoorPassword(String value) {
		this.backdoorPassword = value;
	}
}
