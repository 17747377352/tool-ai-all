/**
 * 功能类型配置
 * 统一管理所有功能类型编号和名称
 */

// 功能类型枚举
export const FUNCTION_TYPE = {
    WATERMARK_REMOVAL: 1,      // 去水印
    IMAGE_GENERATE: 2,         // 生成图片
    OLD_PHOTO_RESTORE: 3,      // 老照片修复
    IMAGE_RECOGNITION: 4,      // AI识图+翻译
    INSTANT_TRANSLATE: 5       // 即时翻译
};

// 功能类型名称映射
export const FUNCTION_TYPE_NAME = {
    [FUNCTION_TYPE.WATERMARK_REMOVAL]: '去水印',
    [FUNCTION_TYPE.IMAGE_GENERATE]: '生成图片',
    [FUNCTION_TYPE.OLD_PHOTO_RESTORE]: '老照片修复',
    [FUNCTION_TYPE.IMAGE_RECOGNITION]: 'AI识图+翻译',
    [FUNCTION_TYPE.INSTANT_TRANSLATE]: '即时翻译'
};

// 功能类型路由映射
export const FUNCTION_TYPE_ROUTE = {
    [FUNCTION_TYPE.WATERMARK_REMOVAL]: '/pages/watermark-removal/watermark-removal',
    [FUNCTION_TYPE.IMAGE_GENERATE]: '/pages/image-generate/image-generate',
    [FUNCTION_TYPE.OLD_PHOTO_RESTORE]: '/pages/old-photo/old-photo',
    [FUNCTION_TYPE.IMAGE_RECOGNITION]: '/pages/image-recognition/image-recognition',
    [FUNCTION_TYPE.INSTANT_TRANSLATE]: '/pages/translate/translate'
};

export default {
    FUNCTION_TYPE,
    FUNCTION_TYPE_NAME,
    FUNCTION_TYPE_ROUTE
};

