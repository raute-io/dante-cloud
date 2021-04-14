package cn.herodotus.eurynome.integration.compliance.domain.aliyun.voice;

import cn.herodotus.eurynome.integration.compliance.domain.aliyun.base.AbstractResultResponse;

/**
 * <p>Description: 阿里音频审核同步返回值对应实体 </p>
 *
 * @author : gengwei.zheng
 * @date : 2021/4/14 14:18
 */
public class VoiceSyncResponse extends AbstractResultResponse<VoiceResult> {

    /**
     * 检测对象的URL。
     */
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
