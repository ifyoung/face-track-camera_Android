package face.camera.beans.record.other;


import face.camera.beans.record.filters.gpuFilters.baseFilter.GPUImageFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicAntiqueFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicBrannanFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicCoolFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicFreudFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicHefeFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicHudsonFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicInkwellFilter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicN1977Filter;
import face.camera.beans.record.filters.gpuFilters.baseFilter.MagicNashvilleFilter;

import static face.camera.beans.record.other.MagicFilterType.ANTIQUE;
import static face.camera.beans.record.other.MagicFilterType.BRANNAN;
import static face.camera.beans.record.other.MagicFilterType.COOL;
import static face.camera.beans.record.other.MagicFilterType.FREUD;
import static face.camera.beans.record.other.MagicFilterType.HEFE;
import static face.camera.beans.record.other.MagicFilterType.HUDSON;
import static face.camera.beans.record.other.MagicFilterType.INKWELL;
import static face.camera.beans.record.other.MagicFilterType.N1977;
import static face.camera.beans.record.other.MagicFilterType.NASHVILLE;
import static face.camera.beans.record.other.MagicFilterType.WARM;

public class MagicFilterFactory {

    private static int filterType = MagicFilterType.NONE;

    public static GPUImageFilter initFilters(int type) {
//        if (type == null) {
//            return null;
//        }
        filterType = type;
        if (type == (BRANNAN)) {
            return new MagicBrannanFilter();
        } else if (type == (ANTIQUE)) {
            return new MagicAntiqueFilter();

        } else if (type == (FREUD)) {
            return new MagicFreudFilter();

        } else if (type == (HEFE)) {
            return new MagicHefeFilter();

        } else if (type == (HUDSON)) {
            return new MagicHudsonFilter();

        } else if (type == (INKWELL)) {
            return new MagicInkwellFilter();

        } else if (type == (N1977)) {
            return new MagicN1977Filter();

        } else if (type == (NASHVILLE)) {
            return new MagicNashvilleFilter();

        } else if (type == (COOL)) {
            return new MagicCoolFilter();

        } else if (type == (WARM)) {
            return new MagicWarmFilter();

        }
        return null;
    }


    private static class MagicWarmFilter extends GPUImageFilter {
    }
}
