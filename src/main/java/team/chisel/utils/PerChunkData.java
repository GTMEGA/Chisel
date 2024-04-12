package team.chisel.utils;

import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;

import lombok.val;
import team.chisel.Chisel;
import team.chisel.api.chunkdata.ChunkData;
import team.chisel.api.chunkdata.IChunkData;
import team.chisel.api.chunkdata.IChunkDataRegistry;
import team.chisel.network.PacketHandler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import team.chisel.proxy.ClientProxy;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public enum PerChunkData implements IChunkDataRegistry {
	INSTANCE;

	public static class MessageChunkData implements IMessage {

		private ChunkCoordIntPair chunk;
		private String key;
		private NBTTagCompound tag;

		public MessageChunkData() {
		}

		public MessageChunkData(Chunk chunk, String key, NBTTagCompound tag) {
			this.chunk = chunk.getChunkCoordIntPair();
			this.key = key;
			this.tag = tag;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(chunk.chunkXPos);
			buf.writeInt(chunk.chunkZPos);
			ByteBufUtils.writeUTF8String(buf, key);
			ByteBufUtils.writeTag(buf, tag);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.chunk = new ChunkCoordIntPair(buf.readInt(), buf.readInt());
			this.key = ByteBufUtils.readUTF8String(buf);
			this.tag = ByteBufUtils.readTag(buf);
		}
	}

	public static class MessageChunkDataHandler implements IMessageHandler<MessageChunkData, IMessage> {

		private static void handle(WorldClient world, MessageChunkData message) {
			if (world == null)
				return;
			Chunk chunk = world.getChunkFromChunkCoords(message.chunk.chunkXPos, message.chunk.chunkZPos);
			if (chunk == null)
				return;
			IChunkData<?> data = INSTANCE.data.get(message.key);
			if (data == null)
				return;
			data.readFromNBT(chunk, message.tag);
			int x = chunk.xPosition << 4;
			int z = chunk.zPosition << 4;
			world.markBlockRangeForRenderUpdate(x, 0, z, x, 255, z);
		}
		@Override
		public IMessage onMessage(MessageChunkData message, MessageContext ctx) {
			val world = Chisel.proxy.getClientWorld();
			if (world == null) {
				((ClientProxy)Chisel.proxy).addDeferredTask((w) -> handle(w, message));
			} else {
				handle((WorldClient) world, message);
			}
			return null;
		}
	}

	/**
	 * @param <T>
	 *            MUST have a default constructor.
	 */
	public static class ChunkDataBase<T extends NBTSaveable> implements IChunkData<T> {

		protected final Table<Integer, ChunkCoordIntPair, T> data = HashBasedTable.create();
		protected final Class<? extends T> clazz;
		private final boolean needsClientSync;

		public ChunkDataBase(Class<? extends T> clazz, boolean needsClientSync) {
			this.clazz = clazz;
			this.needsClientSync = needsClientSync;
		}

		@Override
		public void writeToNBT(Chunk chunk, NBTTagCompound tag) {
			T t = data.get(chunk.worldObj.provider.dimensionId, chunk.getChunkCoordIntPair());
			if (t != null) {
				t.write(tag);
			}
		}

		@Override
		public void readFromNBT(Chunk chunk, NBTTagCompound tag) {
			int dimID = chunk.worldObj.provider.dimensionId;
			ChunkCoordIntPair coords = chunk.getChunkCoordIntPair();
			if (tag.hasNoTags()) {
				data.remove(dimID, coords);
				return;
			}
			T t = getOrCreateNew(dimID, coords);
			t.read(tag);
		}
		
		protected T getOrCreateNew(int dimID, ChunkCoordIntPair coords) {
			T t = data.get(dimID, coords);
			if (t == null) {
				try {
					t = clazz.newInstance();
				} catch (Exception e) {
					Chisel.logger.error("Could not instantiate NBTSaveable " + clazz.getName() + "!");
					e.printStackTrace();
				}
			}
			data.put(dimID, coords, t);
			return t;
		}

		@Override
		public boolean requiresClientSync() {
			return needsClientSync;
		}

		@Override
		public T getDataForChunk(int dimID, ChunkCoordIntPair coords) {
			return getOrCreateNew(dimID, coords);
		}
	}

	private PerChunkData() {
		ChunkData.setOffsetRegistry(this);
	}

	private Map<String, IChunkData<?>> data = Maps.newHashMap();

	public void registerChunkData(String key, IChunkData<?> cd) {
		data.put(key, cd);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IChunkData<?>> T getData(String key) {
		return (T) data.get(key);
	}

	@SubscribeEvent
	public void onChunkSave(ChunkDataEvent.Save event) {
		for (Entry<String, IChunkData<?>> e : data.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			e.getValue().writeToNBT(event.getChunk(), tag);
			event.getData().setTag("chisel:" + e.getKey(), tag);
		}
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkDataEvent.Load event) {
		for (Entry<String, IChunkData<?>> e : data.entrySet()) {
			NBTTagCompound tag = event.getData().getCompoundTag("chisel:" + e.getKey());
			e.getValue().readFromNBT(event.getChunk(), tag);
			updateClient(event.getChunk(), e.getKey(), e.getValue());
		}
	}

	public void chunkModified(Chunk chunk, String key) {
		IChunkData<?> cd = data.get(key);
		chunk.isModified = true;
		updateClient(chunk, key, cd);
	}
	
	private void updateClient(Chunk chunk, String key, IChunkData<?> cd) {
		if (cd.requiresClientSync()) {
			NBTTagCompound tag = new NBTTagCompound();
			cd.writeToNBT(chunk, tag);
			PacketHandler.INSTANCE.sendToAll(new MessageChunkData(chunk, key, tag));
		}
	}
}
