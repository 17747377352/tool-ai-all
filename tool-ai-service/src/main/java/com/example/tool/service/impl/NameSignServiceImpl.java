package com.example.tool.service.impl;

import com.example.tool.service.NameSignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 签名生成服务实现类
 * 使用Java Graphics2D生成姓氏签名图片，支持多种字体风格
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
public class NameSignServiceImpl implements NameSignService {

    // 图片尺寸
    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 400;
    
    // 字体大小
    private static final int FONT_SIZE = 200;

    /**
     * 生成签名图片
     * 
     * @param surname 姓氏
     * @param style 风格：classic-经典, cursive-行书, grass-草书, artistic-艺术
     * @return 签名图片的BufferedImage
     */
    @Override
    public BufferedImage generateSignImage(String surname, String style) {
        // 创建图片
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置背景为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        // 设置字体
        Font font = getFontByStyle(style);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        
        // 计算文字位置（居中）
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(surname);
        int textHeight = fm.getHeight();
        int x = (IMAGE_WIDTH - textWidth) / 2;
        int y = (IMAGE_HEIGHT + textHeight) / 2 - fm.getDescent();
        
        // 绘制文字
        g2d.drawString(surname, x, y);
        
        // 根据风格添加效果
        applyStyleEffect(g2d, style, surname, x, y, textWidth, textHeight);
        
        g2d.dispose();
        
        log.info("签名图片生成成功: 姓氏={}, 风格={}", surname, style);
        return image;
    }

    /**
     * 根据风格获取字体
     */
    private Font getFontByStyle(String style) {
        Font baseFont = new Font("SimSun", Font.PLAIN, FONT_SIZE); // 默认宋体
        
        switch (style) {
            case "classic":
                // 经典风格：使用宋体加粗
                return new Font("SimSun", Font.BOLD, FONT_SIZE);
            case "cursive":
                // 行书风格：使用楷体，倾斜
                Font kaiFont = new Font("KaiTi", Font.ITALIC, FONT_SIZE);
                if (kaiFont.getFamily().equals("KaiTi")) {
                    return kaiFont;
                }
                // 如果没有楷体，使用斜体
                return new Font("SimSun", Font.ITALIC, FONT_SIZE);
            case "grass":
                // 草书风格：使用行楷，更倾斜
                Font xingkaiFont = new Font("STXingkai", Font.ITALIC, (int)(FONT_SIZE * 0.9));
                if (xingkaiFont.getFamily().equals("STXingkai")) {
                    return xingkaiFont;
                }
                return new Font("SimSun", Font.ITALIC, (int)(FONT_SIZE * 0.9));
            case "artistic":
                // 艺术风格：使用黑体，加粗
                Font heiFont = new Font("SimHei", Font.BOLD, FONT_SIZE);
                if (heiFont.getFamily().equals("SimHei")) {
                    return heiFont;
                }
                return new Font("SimSun", Font.BOLD, FONT_SIZE);
            default:
                return baseFont;
        }
    }

    /**
     * 应用风格效果
     */
    private void applyStyleEffect(Graphics2D g2d, String style, String text, int x, int y, int width, int height) {
        switch (style) {
            case "artistic":
                // 艺术风格：添加阴影效果
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawString(text, x + 3, y + 3);
                g2d.setColor(Color.BLACK);
                break;
            case "cursive":
            case "grass":
                // 行书/草书：添加轻微的装饰线
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawLine(x, y + height / 2, x + width, y + height / 2);
                g2d.setColor(Color.BLACK);
                break;
            default:
                // 经典风格：无额外效果
                break;
        }
    }
}

