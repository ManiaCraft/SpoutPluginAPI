/*
 * This file is part of SpoutPluginAPI (http://www.spout.org/).
 *
 * SpoutPluginAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SpoutPluginAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.getspout.spoutapi.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.getspout.commons.io.FileUtil;

public class PacketCacheFile implements CompressablePacket {
	private String plugin;
	private byte[] fileData;
	private String fileName;
	private boolean compressed = false;

	public PacketCacheFile() {

	}

	public PacketCacheFile(String plugin, File file) {
		this.plugin = plugin;
		try {
			this.fileData = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.fileName = FileUtil.getFileName(file.getPath());
	}

	//TODO move to separate thread?
	public void compress() {
		if (!compressed) {
			Deflater deflater = new Deflater();
			deflater.setInput(fileData);
			deflater.setLevel(Deflater.BEST_COMPRESSION);
			deflater.finish();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(fileData.length);
			byte[] buffer = new byte[1024];
			while (!deflater.finished()) {
				int bytesCompressed = deflater.deflate(buffer);
				bos.write(buffer, 0, bytesCompressed);
			}
			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fileData = bos.toByteArray();
			compressed = true;
		}
	}

	public void decompress() {
		if (compressed) {
			Inflater decompressor = new Inflater();
			decompressor.setInput(fileData);

			ByteArrayOutputStream bos = new ByteArrayOutputStream(fileData.length);

			byte[] buf = new byte[1024];
			while (!decompressor.finished()) {
				try {
					int count = decompressor.inflate(buf);
					bos.write(buf, 0, count);
				} catch (DataFormatException e) {

				}
			}
			try {
				bos.close();
			} catch (IOException e) {

			}

			fileData = bos.toByteArray();
		}
	}

	public boolean isCompressed() {
		return compressed;
	}

	@Override
	public int getNumBytes() {
		return PacketUtil.getNumBytes(fileName) + PacketUtil.getNumBytes(plugin) + fileData.length + 4;
	}

	@Override
	public void readData(DataInputStream input) throws IOException {
		this.fileName = PacketUtil.readString(input);
		this.plugin = PacketUtil.readString(input);
		compressed = input.readBoolean();
		int size = input.readInt();
		this.fileData = new byte[size];
		input.readFully(fileData);
	}

	@Override
	public void writeData(DataOutputStream output) throws IOException {
		PacketUtil.writeString(output, fileName);
		PacketUtil.writeString(output, plugin);
		output.writeBoolean(compressed);
		output.writeInt(fileData.length);
		output.write(fileData);
	}

	@Override
	public void run(int playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void failure(int playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public PacketType getPacketType() {
		return PacketType.PacketCacheFile;
	}

	@Override
	public int getVersion() {
		return 1;
	}
}
