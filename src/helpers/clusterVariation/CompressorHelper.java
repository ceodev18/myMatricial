package helpers.clusterVariation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressorHelper {
	public static String compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return new String(compressed, StandardCharsets.UTF_8);
	}

	public static String decompress(byte[] compressed) throws IOException {
		// public static String decompress(String someShit) {
		// byte[] someShitByte = someShit.getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
		GZIPInputStream gis = new GZIPInputStream(bis);
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		gis.close();
		bis.close();
		return sb.toString();
	}
}
