package cn.kanyun.log.util;

/**
 * This class represents an IP Range, which are represented by an IP address and and a subnet mask. The standards
 * describing modern routing protocols often refer to the extended-network-prefix-length rather than the subnet mask.
 * The prefix length is equal to the number of contiguous one-bits in the traditional subnet mask. This means that
 * specifying the network address 130.5.5.25 with a subnet mask of 255.255.255.0 can also be expressed as 130.5.5.25/24.
 * The prefix-length notation is more compact and easier to understand than writing out the mask in its traditional
 * dotted-decimal format.
 * 此类表示IP范围，该范围由IP地址和子网掩码表示。标准描述现代路由协议通常是指扩展的网络前缀长度，而不是子网掩码
 * 前缀长度等于传统子网掩码中相邻的1位数。这意味着指定子网掩码为255.255.255.0的网络地址130.5.5.25也可以表示为130.5.5.25/24。
 * 前缀长度表示法比用传统的点分十进制格式。
 *
 * @author kanyun
 * @version 1.0
 * @see IPAddress
 */
public class IPRange {

    /**
     * IP address
     */
    private IPAddress ipAddress = null;

    /**
     * IP subnet mask
     * IP子网掩码
     */
    private IPAddress ipSubnetMask = null;

    /**
     * extended network prefix
     * 扩展网络前缀
     */
    private int extendedNetworkPrefix = 0;

    public IPRange(String range) {
        parseRange(range);
    }

    // -------------------------------------------------------------------------

    /**
     * Return the encapsulated IP address.
     * 返回封装的IP地址。
     *
     * @return The IP address.
     */
    public final IPAddress getIPAddress() {
        return ipAddress;
    }

    // -------------------------------------------------------------------------

    /**
     * Return the encapsulated subnet mask
     * 返回封装IP子网掩码
     *
     * @return The IP range's subnet mask.
     */
    public final IPAddress getIPSubnetMask() {
        return ipSubnetMask;
    }

    // -------------------------------------------------------------------------

    /**
     * Return the extended extended network prefix.
     * 返回扩展网络前缀
     *
     * @return Return the extended network prefix.
     */
    public final int getExtendedNetworkPrefix() {
        return extendedNetworkPrefix;
    }

    // -------------------------------------------------------------------------

    /**
     * Convert the IP Range into a string representation.
     * 将IP范围转换为字符串表示形式
     *
     * @return Return the string representation of the IP Address following the common format xxx.xxx.xxx.xxx/xx (IP
     * address/extended network prefixs).
     */
    @Override
    public String toString() {
        return ipAddress.toString() + "/" + extendedNetworkPrefix;
    }

    // -------------------------------------------------------------------------

    /**
     * Parse the IP range string representation.
     * 解析IP范围字符串表示
     *
     * @param range String representation of the IP range.
     * @throws IllegalArgumentException Throws this exception if the specified range is not a valid IP network range.
     */
    final void parseRange(String range) {
        if (range == null) {
            throw new IllegalArgumentException("Invalid IP range");
        }

//        判断IP字符串是否"/",如果包含,说明是一个IP范围
        int index = range.indexOf('/');
        String subnetStr = null;
        if (index == -1) {
            ipAddress = new IPAddress(range);
        } else {
            ipAddress = new IPAddress(range.substring(0, index));
            subnetStr = range.substring(index + 1);
        }

        // try to convert the remaining part of the range into a decimal
        // value.
        try {
            if (subnetStr != null) {
                extendedNetworkPrefix = Integer.parseInt(subnetStr);
                if ((extendedNetworkPrefix < 0) || (extendedNetworkPrefix > 32)) {
                    throw new IllegalArgumentException("Invalid IP range [" + range + "]");
                }
                ipSubnetMask = computeMaskFromNetworkPrefix(extendedNetworkPrefix);
            }
        } catch (NumberFormatException ex) {
            // 这段代码是捕捉前面Integer.parseInt(subnetStr)
            // 剩余部分不是有效的十进制值.
            // Check if it's a decimal-dotted notation.
            // 检查它是否是小数点符号,即是否是小数
            ipSubnetMask = new IPAddress(subnetStr);

            // create the corresponding subnet decimal
            // 创建相应的子网十进制
            extendedNetworkPrefix = computeNetworkPrefixFromMask(ipSubnetMask);
            if (extendedNetworkPrefix == -1) {
                throw new IllegalArgumentException("Invalid IP range [" + range + "]", ex);
            }
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Compute the extended network prefix from the IP subnet mask.
     * 从IP子网掩码计算扩展网络前缀
     *
     * @param mask Reference to the subnet mask IP number.
     * @return Return the extended network prefix. Return -1 if the specified mask cannot be converted into a extended
     * prefix network.
     */
    private int computeNetworkPrefixFromMask(IPAddress mask) {

        int result = 0;
        int tmp = mask.getIPAddress();

        while ((tmp & 0x00000001) == 0x00000001) {
            result++;
            tmp = tmp >>> 1;
        }

        if (tmp != 0) {
            return -1;
        }

        return result;
    }

    public static String toDecimalString(String inBinaryIpAddress) {
        StringBuilder decimalIp = new StringBuilder();
        String[] binary = new String[4];

        for (int i = 0, c = 0; i < 32; i = i + 8, c++) {
            binary[c] = inBinaryIpAddress.substring(i, i + 8);
            int octet = Integer.parseInt(binary[c], 2);
            decimalIp.append(octet);
            if (c < 3) {

                decimalIp.append('.');
            }
        }
        return decimalIp.toString();
    }

    // -------------------------------------------------------------------------

    /**
     * Convert a extended network prefix integer into an IP number.
     * 将扩展网络前缀整数转换为IP号
     *
     * @param prefix The network prefix number.
     * @return Return the IP number corresponding to the extended network prefix.
     */
    private IPAddress computeMaskFromNetworkPrefix(int prefix) {

        /*
         * int subnet = 0; for (int i=0; i<prefix; i++) { subnet = subnet << 1; subnet += 1; }
         */

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (i < prefix) {
                str.append("1");
            } else {
                str.append("0");
            }
        }

        String decimalString = toDecimalString(str.toString());
        return new IPAddress(decimalString);

    }

    // -------------------------------------------------------------------------

    /**
     * Check if the specified IP address is in the encapsulated range.
     * 检查指定的IP地址是否在封装范围内
     *
     * @param address The IP address to be tested.
     * @return Return <code>true</code> if the specified IP address is in the encapsulated IP range, otherwise return
     * <code>false</code>.
     */
    public boolean isIPAddressInRange(IPAddress address) {
        if (ipSubnetMask == null) {
            return this.ipAddress.equals(address);
        }

        int result1 = address.getIPAddress() & ipSubnetMask.getIPAddress();
        int result2 = ipAddress.getIPAddress() & ipSubnetMask.getIPAddress();

        return result1 == result2;
    }

}
