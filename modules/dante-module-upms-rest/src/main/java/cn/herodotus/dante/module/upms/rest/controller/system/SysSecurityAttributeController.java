/*
 * Copyright (c) 2020-2030 ZHENGGENGWEI(码匠君)<herodotus@aliyun.com>
 *
 * Dante Cloud licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Dante Cloud 采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改 Dante Cloud 源码头部的版权声明。
 * 3.请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://gitee.com/dromara/dante-cloud
 * 5.在修改包名，模块名称，项目代码等时，请注明软件出处 https://gitee.com/dromara/dante-cloud
 * 6.若您的项目无法满足以上几点，可申请商业授权
 */

package cn.herodotus.dante.module.upms.rest.controller.system;

import cn.herodotus.dante.module.upms.logic.entity.system.SysSecurityAttribute;
import cn.herodotus.dante.module.upms.logic.service.system.SysSecurityAttributeService;
import cn.herodotus.engine.data.core.service.WriteableService;
import cn.herodotus.engine.rest.core.controller.BaseWriteableRestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Description: SysSecurityAttributeController </p>
 *
 * @author : gengwei.zheng
 * @date : 2022/6/20 18:12
 */
@RestController
@RequestMapping("/security-attribute")
@Tags({
        @Tag(name = "用户安全管理接口"),
        @Tag(name = "系统元数据管理接口")
})
public class SysSecurityAttributeController extends BaseWriteableRestController<SysSecurityAttribute, String> {

    private final SysSecurityAttributeService sysSecurityAttributeService;

    @Autowired
    public SysSecurityAttributeController(SysSecurityAttributeService sysSecurityAttributeService) {
        this.sysSecurityAttributeService = sysSecurityAttributeService;
    }

    @Override
    public WriteableService<SysSecurityAttribute, String> getWriteableService() {
        return this.sysSecurityAttributeService;
    }
}