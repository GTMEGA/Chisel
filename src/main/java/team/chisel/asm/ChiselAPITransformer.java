package team.chisel.asm;

import lombok.val;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ChiselAPITransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null)
            return null;

        val reader = new ClassReader(bytes);
        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        val remapAdapter = new ChiselAPIRemappingAdapter(writer);
        reader.accept(remapAdapter, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }
}
