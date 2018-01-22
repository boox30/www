package com.onyx.jdread.reader.menu.request;


import com.onyx.android.sdk.reader.utils.ImageUtils;
import com.onyx.jdread.reader.common.GammaInfo;
import com.onyx.jdread.reader.data.Reader;
import com.onyx.jdread.reader.request.ReaderBaseRequest;

/**
 * Created by zhuzeng on 10/5/15.
 * redraw request
 */
public class GammaCorrectionRequest extends ReaderBaseRequest {
    private Reader reader;
    private GammaInfo gammaInfo;

    public GammaCorrectionRequest(final Reader reader, GammaInfo gammaInfo) {
        this.gammaInfo = gammaInfo;
        this.reader = reader;
    }

    @Override
    public GammaCorrectionRequest call() throws Exception {
        if (reader.getReaderHelper().getRenderer().getRendererFeatures().supportFontGammaAdjustment()) {
            float gamma = ImageUtils.getGammaCorrectionBySelection(gammaInfo.getTextGamma());
            reader.getReaderHelper().getRenderer().setTextGamma(gamma);
        }
        reader.getReaderViewHelper().updatePageView(reader,getReaderUserDataInfo(),getReaderViewInfo());
        return this;
    }
}