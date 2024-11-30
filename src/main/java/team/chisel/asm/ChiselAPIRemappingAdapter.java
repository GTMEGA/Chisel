package team.chisel.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import team.chisel.Tags;

public class ChiselAPIRemappingAdapter extends RemappingClassAdapter {
    public ChiselAPIRemappingAdapter(ClassVisitor cv) {
        super(cv, ChiselAPIRemapper.INSTANCE);
    }

    private static class ChiselAPIRemapper extends Remapper {
        public static final ChiselAPIRemapper INSTANCE = new ChiselAPIRemapper();
        private static final String OLD_API_PREFIX = "com/cricketcraft/chisel/";
        private static final int OLD_API_PREFIX_LENGTH = OLD_API_PREFIX.length();
        private static final String NEW_API_PREFIX = Tags.ROOT_PKG.replace('.', '/') + "/";
        @Override
        public String map(String typeName) {
            if (typeName.startsWith(OLD_API_PREFIX)) {
                return NEW_API_PREFIX + typeName.substring(OLD_API_PREFIX_LENGTH);
            } else {
                return typeName;
            }
        }
    }
}
