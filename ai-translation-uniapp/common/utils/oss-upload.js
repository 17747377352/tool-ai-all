/**
 * OSS直传工具类
 * 使用OSS PostObject方式直接上传文件到阿里云OSS
 */
import api from './api.js';

/**
 * 上传文件到OSS
 * @param {String} filePath 文件路径（uni.chooseImage返回的tempFilePath）
 * @param {String} fileName 文件名（可选，不传则自动生成）
 * @returns {Promise<String>} 返回OSS文件URL
 */
export async function uploadToOss(filePath, fileName) {
    try {
        // 如果没有指定文件名，自动生成
        if (!fileName) {
            const timestamp = Date.now();
            const random = Math.random().toString(36).substring(2, 8);
            const date = new Date();
            const dateStr = `${date.getFullYear()}${String(date.getMonth() + 1).padStart(2, '0')}${String(date.getDate()).padStart(2, '0')}`;
            const ext = filePath.substring(filePath.lastIndexOf('.'));
            fileName = `upload/${dateStr}/${timestamp}_${random}${ext}`;
        }
        
        // 从后端获取OSS PostObject签名
        const res = await api.getPostObjectSignature(fileName);
        
        if (res.code !== 200 || !res.data) {
            throw new Error('获取签名失败');
        }
        
        const signature = res.data;
        const host = signature.host;
        const key = signature.key || fileName;
        
        // 使用uni.uploadFile直接上传到OSS
        return new Promise((resolve, reject) => {
            uni.uploadFile({
                url: `https://${host}`,
                filePath: filePath,
                name: 'file',
                formData: {
                    'key': key,
                    'policy': signature.policy,
                    'OSSAccessKeyId': signature.accessKeyId,
                    'signature': signature.signature,
                    'success_action_status': '200'
                },
                success: (uploadRes) => {
                    if (uploadRes.statusCode === 200 || uploadRes.statusCode === 204) {
                        // 构建OSS文件URL
                        const fileUrl = `https://${host}/${key}`;
                        resolve(fileUrl);
                    } else {
                        reject(new Error('上传失败，状态码: ' + uploadRes.statusCode));
                    }
                },
                fail: (error) => {
                    console.error('上传文件到OSS失败:', error);
                    reject(new Error('上传失败: ' + (error.errMsg || '未知错误')));
                }
            });
        });
    } catch (error) {
        console.error('上传文件到OSS失败:', error);
        throw new Error('上传失败: ' + (error.message || '未知错误'));
    }
}

