package me.marquez.socket.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressUtil {
    public static byte[] compress(byte[] data) throws IOException {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            // Zip 파일 내에 들어갈 엔트리 생성
            ZipEntry entry = new ZipEntry("data");
            zipOutputStream.putNextEntry(entry);

            // 데이터를 ZipOutputStream에 쓰기
            zipOutputStream.write(data);

            // 엔트리 및 스트림 닫기
            zipOutputStream.closeEntry();
            zipOutputStream.close();

            // 압축된 데이터를 byte 배열로 반환
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static byte[] decompress(byte[] compressedData) throws IOException {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
            ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            // 첫 번째 Zip 엔트리 가져오기
            ZipEntry entry = zipInputStream.getNextEntry();

            if (entry != null) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
            }

            // 스트림 닫기
            zipInputStream.closeEntry();
            zipInputStream.close();
            byteArrayOutputStream.close();

            return byteArrayOutputStream.toByteArray();
        }
    }
}
