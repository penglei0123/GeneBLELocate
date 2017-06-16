package com.genepoint.beacon.utils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EddyStone {
    public static final Map<Byte, String> BYTE_EXPANSIONS;
    static HashMap<Byte, String> urlSchemes;
    private static final ConcurrentHashMap<String, Integer> UrlPrefixKeyMap;
    private static final ConcurrentHashMap<String, Integer> UrlSuffixKeyMap;
    public static final Map<Byte, String> URL_SCHEMES;


    static {
        HashMap<Byte, String> byteExpansions = new HashMap();
        byteExpansions.put(Byte.valueOf((byte) 0), ".com/");
        byteExpansions.put(Byte.valueOf((byte) 1), ".org/");
        byteExpansions.put(Byte.valueOf((byte) 2), ".edu/");
        byteExpansions.put(Byte.valueOf((byte) 3), ".net/");
        byteExpansions.put(Byte.valueOf((byte) 4), ".info/");
        byteExpansions.put(Byte.valueOf((byte) 5), ".biz/");
        byteExpansions.put(Byte.valueOf((byte) 6), ".gov/");
        byteExpansions.put(Byte.valueOf((byte) 7), ".com");
        byteExpansions.put(Byte.valueOf((byte) 8), ".org");
        byteExpansions.put(Byte.valueOf((byte) 9), ".edu");
        byteExpansions.put(Byte.valueOf((byte) 10), ".net");
        byteExpansions.put(Byte.valueOf((byte) 11), ".info");
        byteExpansions.put(Byte.valueOf((byte) 12), ".biz");
        byteExpansions.put(Byte.valueOf((byte) 13), ".gov");
        BYTE_EXPANSIONS = Collections.unmodifiableMap(byteExpansions);

        UrlPrefixKeyMap = new ConcurrentHashMap();
        Iterator iter  = BYTE_EXPANSIONS.entrySet().iterator();

        Map.Entry entry;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            UrlPrefixKeyMap.put((String)entry.getValue(), Integer.valueOf(((Byte) entry.getKey()).byteValue() & 255));
        }

        urlSchemes = new HashMap();
        urlSchemes.put(Byte.valueOf((byte) 0), "http://www.");
        urlSchemes.put(Byte.valueOf((byte) 1), "https://www.");
        urlSchemes.put(Byte.valueOf((byte) 2), "http://");
        urlSchemes.put(Byte.valueOf((byte) 3), "https://");
        UrlSuffixKeyMap = new ConcurrentHashMap();
        iter = urlSchemes.entrySet().iterator();

        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            UrlSuffixKeyMap.put((String)entry.getValue(), Integer.valueOf(((Byte) entry.getKey()).byteValue() & 255));
        }

        URL_SCHEMES = Collections.unmodifiableMap(urlSchemes);

    }


    public static boolean isEddyStone(byte[] bytes) {
        if (bytes != null)
            if (String.format("%02X%02X", new Object[]{Byte.valueOf(bytes[5]), Byte.valueOf(bytes[6])}).equals("AAFE"))
                return true;
        return false;
    }

    public static boolean isiBeacon(byte[] bytes) {
        return (bytes != null) && (bytes[5] == 76) && (bytes[6] == 0);
    }

    public static boolean isTelemetryPacket(byte[] scanRecord) {
        return (scanRecord[11] & 0xF0) == 32;
    }

    public static boolean isUidPacket(byte[] scanRecord) {
        return (scanRecord[11] & 0xF0) == 0;
    }

    public static boolean isUrlPacket(byte[] scanRecord) {
        return (scanRecord[11] & 0xF0) == 16;
    }

    public static byte[] of(byte[] data, int offset, int byteCount) {
        if (data == null)
            throw new IllegalArgumentException("data == null");
        Util.checkOffsetAndCount(data.length, offset, byteCount);

        byte[] copy = new byte[byteCount];
        System.arraycopy(data, offset, copy, 0, byteCount);
        return copy;
    }

    public static String url(byte[] scanRecord, int len) {
        if (scanRecord.length < 3) {
            return "";
        }
        return urlScheme((byte) (scanRecord[2] & 0xF)) + encodedUrl(scanRecord, 3, len);
    }

    public static String urlV2(byte[] scanRecord) {
        return scanRecord.length >= 3 && scanRecord[1] != 0 ? encodedUrlV2(scanRecord) : "";
    }

    public static String urlScheme(byte schemeByte) {
        try {
            Preconditions.checkArgument(URL_SCHEMES.containsKey(Byte.valueOf(schemeByte)), String.format("Unknown url scheme, byte: %X", new Object[]{Byte.valueOf(schemeByte)}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (String) URL_SCHEMES.get(Byte.valueOf(schemeByte));
    }

    public static String encodedUrl(byte[] serviceData, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < serviceData.length; i++) {
            if (i - offset == len - 1) {
                break;
            }
            String expansion = urlExpansionsForByte(serviceData[i]);
            if (expansion != null) {
                sb.append(expansion);
            } else {
                sb.append((char) serviceData[i]);
            }
        }
        return sb.toString();
    }

    public static String encodedUrl(byte[] serviceData) {
        if (serviceData == null) {
            return null;
        }
        if (serviceData.length < 3) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        sb.append(urlScheme((byte) (serviceData[0] & 0xF)));

        for (int i = 1; i < serviceData.length; i++) {
            String expansion = urlExpansionsForByte(serviceData[i]);
            if (expansion != null) {
                sb.append(expansion);
            } else {
                sb.append((char) serviceData[i]);
            }
        }
        return sb.toString();
    }

    public static String encodedUrlV2(byte[] serviceData) {
        if (serviceData == null) {
            return null;
        } else if (serviceData.length < 3) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            int prefix_id = serviceData[0] & 255;
            boolean url_body_start = false;
            String url_prefix = (String) URL_SCHEMES.get(Byte.valueOf((byte) prefix_id));
            if (url_prefix == null) {
                sb.append((new String(serviceData, 0, serviceData.length)).trim());
                return sb.toString();
            } else {
                sb.append(url_prefix);
                byte var9 = 1;
                String url_suffix = null;
                int url_body_end = serviceData.length;

                for (int i = var9 + 1; i < serviceData.length; ++i) {
                    int suffix_id = serviceData[i] & 255;
                    url_suffix = (String) BYTE_EXPANSIONS.get(Byte.valueOf((byte) suffix_id));
                    if (url_suffix != null) {
                        url_body_end = i;
                        break;
                    }
                }

                System.out.println(url_body_end);
                sb.append(new String(serviceData, var9, url_body_end - var9));
                if (url_suffix != null) {
                    sb.append(url_suffix);
                }

                return sb.toString();
            }
        }
    }

    public static byte[] urlToBytes(String url) {
        Preconditions.checkNotNull(url, "Url cannot be null");
        Preconditions.checkArgument(doesStartWithValidScheme(url), "URL needs to start with " + URL_SCHEMES.values());

        for (Map.Entry<Byte, String> entry : URL_SCHEMES.entrySet()) {
            if (url.startsWith((String) entry.getValue())) {
                url = url.replace((CharSequence) entry.getValue(), String.valueOf((char) ((Byte) entry.getKey()).byteValue()));
            }
        }

        for (Map.Entry<Byte, String> entry : BYTE_EXPANSIONS.entrySet()) {
            url = url.replace((CharSequence) entry.getValue(), String.valueOf((char) ((Byte) entry.getKey()).byteValue()));
        }

        Preconditions.checkArgument(url.length() <= 17, "URL cannot be bigger than 17 bytes.");
        byte[] bytes = new byte[url.length()];
        for (int i = 0; i < url.length(); i++) {
            bytes[i] = ((byte) url.charAt(i));
        }
        return bytes;
    }

    public static byte[] urlToBytesV2(String url) {
        if (url == null) {
            return new byte[1];
        } else {
            int url_prefix_id = 2147483647;
            int url_prefix_offset = 0;
            Iterator url_suffix_id = URL_SCHEMES.entrySet().iterator();

            while (url_suffix_id.hasNext()) {
                Map.Entry url_suffix_offset = (Map.Entry) url_suffix_id.next();
                int url_data_str = ((Byte) url_suffix_offset.getKey()).byteValue() & 255;
                String url_data = (String) url_suffix_offset.getValue();
                if (url.startsWith(url_data) && url_prefix_id > url_data_str) {
                    url_prefix_id = url_data_str;
                    url_prefix_offset = url_data.length();
                }
            }

            if (url_prefix_id == 2147483647) {
                return new byte[1];
            } else {
                int var9 = 2147483647;
                int var10 = url.length();
                Iterator var11 = BYTE_EXPANSIONS.entrySet().iterator();

                int bufferSize;
                while (var11.hasNext()) {
                    Map.Entry var13 = (Map.Entry) var11.next();
                    bufferSize = ((Byte) var13.getKey()).byteValue() & 255;
                    String buffer = (String) var13.getValue();
                    if (url.endsWith(buffer)) {
                        var9 = bufferSize;
                        var10 = url.length() - buffer.length();
                    }
                }

                String var12 = url.substring(url_prefix_offset, var10);
                System.out.println(var12);
                byte[] var14 = var12.getBytes();
                bufferSize = var14.length;
                if (url_prefix_id != 2147483647) {
                    ++bufferSize;
                }

                if (var9 != 2147483647) {
                    ++bufferSize;
                }

                System.out.println(bufferSize);
                System.out.println(url_prefix_id);
                System.out.println(var9);
                ByteBuffer var15 = ByteBuffer.allocate(bufferSize);
                if (url_prefix_id != 2147483647) {
                    var15.put((byte) url_prefix_id);
                }

                var15.put(var14);
                if (var9 != 2147483647) {
                    var15.put((byte) var9);
                }

                return var15.array();
            }
        }
    }


    private static boolean doesStartWithValidScheme(String url) {
        Iterator var1 = URL_SCHEMES.values().iterator();
        String scheme;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            scheme = (String) var1.next();
        } while (!url.startsWith(scheme));

        return true;
    }

    private static String urlExpansionsForByte(byte character) {
        return (String) BYTE_EXPANSIONS.get(Byte.valueOf(character));
    }

}
