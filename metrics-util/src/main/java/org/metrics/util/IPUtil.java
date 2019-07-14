package org.metrics.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Optional;

public class IPUtil {

	public static Optional<String> getLocalHost() {
		try {
			return Optional.of(InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e) {
		}
		return Optional.empty();
	}

	/**
	 * 
	 * @param prefix
	 * @return
	 */
	public static Optional<String> getHost(String prefix) {
		if (prefix == null) {
			return Optional.empty();
		}
		try {
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while (nics.hasMoreElements()) {
				NetworkInterface nic = (NetworkInterface) nics.nextElement();
				Enumeration<InetAddress> addresses = nic.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress inetAddress = (InetAddress) addresses.nextElement();
					String hostAddress = inetAddress.getHostAddress();
					if (hostAddress.startsWith(prefix)) {
						return Optional.of(hostAddress);
					}
				}
			}
		} catch (Exception e) {

		}
		return Optional.empty();
	}
}
