package com.yookassa.spring.client;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class IPValidator {

    // Официальные IP-адреса ЮKassa для webhook
    private static final List<String> YOOKASSA_IP_RANGES = Arrays.asList(
            "185.71.76.0/27",
            "185.71.77.0/27",
            "77.75.153.0/25",
            "77.75.154.128/25",
            "2a02:5180::/32"
    );

    private static final List<String> YOOKASSA_SINGLE_IPS = Arrays.asList(
            "77.75.156.11",
            "77.75.156.35"
    );

    public boolean isValidYooKassaIP(String clientIP) {
        if (clientIP == null || clientIP.isEmpty()) {
            return false;
        }

        try {
            InetAddress clientAddress = InetAddress.getByName(clientIP);

            // Проверка одиночных IP
            for (String singleIP : YOOKASSA_SINGLE_IPS) {
                if (clientIP.equals(singleIP)) {
                    log.debug("Valid YooKassa IP detected: {}", clientIP);
                    return true;
                }
            }

            // Проверка диапазонов IP
            for (String cidr : YOOKASSA_IP_RANGES) {
                if (isIPInRange(clientAddress, cidr)) {
                    log.debug("Valid YooKassa IP range detected: {} in {}", clientIP, cidr);
                    return true;
                }
            }

            log.warn("Invalid IP address for YooKassa webhook: {}", clientIP);
            return false;

        } catch (Exception e) {
            log.error("Error validating IP address: {}", clientIP, e);
            return false;
        }
    }

    private boolean isIPInRange(InetAddress clientAddress, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress networkAddress = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] clientBytes = clientAddress.getAddress();
            byte[] networkBytes = networkAddress.getAddress();

            if (clientBytes.length != networkBytes.length) {
                return false;
            }

            int bytesToCheck = prefixLength / 8;
            int bitsToCheck = prefixLength % 8;

            // Проверка полных байтов
            for (int i = 0; i < bytesToCheck; i++) {
                if (clientBytes[i] != networkBytes[i]) {
                    return false;
                }
            }

            // Проверка оставшихся битов
            if (bitsToCheck > 0 && bytesToCheck < clientBytes.length) {
                int mask = 0xFF << (8 - bitsToCheck);
                return (clientBytes[bytesToCheck] & mask) == (networkBytes[bytesToCheck] & mask);
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking IP range: {} for CIDR: {}", clientAddress.getHostAddress(), cidr, e);
            return false;
        }
    }
}