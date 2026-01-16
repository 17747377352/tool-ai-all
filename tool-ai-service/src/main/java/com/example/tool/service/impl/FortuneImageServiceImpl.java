package com.example.tool.service.impl;

import com.example.tool.service.FortuneImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 运势图片生成服务实现类
 * 使用Java Graphics2D生成运势和星座运势图片
 * 
 * @author tool-ai-service
 * @since 1.0
 */
@Slf4j
@Service
public class FortuneImageServiceImpl implements FortuneImageService {

    // 图片尺寸
    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 1600; // 增加高度以容纳更多内容
    
    // 边距
    private static final int PADDING = 40;
    
    // 标题字体大小
    private static final int TITLE_FONT_SIZE = 48;
    
    // 正文字体大小
    private static final int CONTENT_FONT_SIZE = 26; // 稍微减小字体以容纳更多内容
    
    // 行间距
    private static final int LINE_SPACING = 8;
    
    // 小标题字体大小
    private static final int SECTION_FONT_SIZE = 32;

    /**
     * 生成运势图片
     * 
     * @param name 姓名
     * @param birthDate 出生日期
     * @param fortuneText 运势文案
     * @return 运势图片
     */
    @Override
    public BufferedImage generateFortuneImage(String name, String birthDate, String fortuneText) {
        // 创建图片
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置背景渐变
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 250, 240),
                0, IMAGE_HEIGHT, new Color(255, 245, 230)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        int currentY = PADDING;
        
        // 绘制标题
        Font titleFont = new Font("SimHei", Font.BOLD, TITLE_FONT_SIZE);
        g2d.setFont(titleFont);
        g2d.setColor(new Color(139, 69, 19)); // 棕色
        
        String title = name + " 运势测试";
        FontMetrics titleFm = g2d.getFontMetrics();
        int titleWidth = titleFm.stringWidth(title);
        int titleX = (IMAGE_WIDTH - titleWidth) / 2;
        g2d.drawString(title, titleX, currentY + titleFm.getAscent());
        currentY += titleFm.getHeight() + 30;
        
        // 绘制出生日期
        Font dateFont = new Font("SimSun", Font.PLAIN, 24);
        g2d.setFont(dateFont);
        g2d.setColor(new Color(128, 128, 128));
        String dateText = "出生日期：" + birthDate;
        FontMetrics dateFm = g2d.getFontMetrics();
        int dateWidth = dateFm.stringWidth(dateText);
        int dateX = (IMAGE_WIDTH - dateWidth) / 2;
        g2d.drawString(dateText, dateX, currentY + dateFm.getAscent());
        currentY += dateFm.getHeight() + 40;
        
        // 绘制分割线
        g2d.setColor(new Color(200, 180, 160));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(PADDING, currentY, IMAGE_WIDTH - PADDING, currentY);
        currentY += 30;
        
        // 绘制运势内容
        Font contentFont = new Font("SimSun", Font.PLAIN, CONTENT_FONT_SIZE);
        Font sectionFont = new Font("SimHei", Font.BOLD, SECTION_FONT_SIZE);
        g2d.setFont(contentFont);
        g2d.setColor(new Color(60, 60, 60));
        
        // 处理文本换行和格式
        List<TextLine> lines = processFortuneText(fortuneText, IMAGE_WIDTH - 2 * PADDING, g2d, contentFont, sectionFont);
        
        FontMetrics contentFm = g2d.getFontMetrics(contentFont);
        FontMetrics sectionFm = g2d.getFontMetrics(sectionFont);
        int contentLineHeight = contentFm.getHeight() + LINE_SPACING;
        int sectionLineHeight = sectionFm.getHeight() + LINE_SPACING + 10;
        
        for (TextLine line : lines) {
            int lineHeight = line.isSection ? sectionLineHeight : contentLineHeight;
            
            // 检查是否超出图片范围
            if (currentY + lineHeight > IMAGE_HEIGHT - 80) {
                log.warn("内容超出图片范围，已截断");
                break;
            }
            log.info("line.text: {}", line.text);
            
            // 设置字体和颜色
            if (line.isSection) {
                g2d.setFont(sectionFont);
                g2d.setColor(new Color(139, 69, 19)); // 棕色
            } else {
                g2d.setFont(contentFont);
                g2d.setColor(new Color(60, 60, 60)); // 深灰色
            }
            
            FontMetrics fm = line.isSection ? sectionFm : contentFm;
            g2d.drawString(line.text, PADDING, currentY + fm.getAscent());
            currentY += lineHeight;
        }
        
        // 绘制底部装饰
        currentY = IMAGE_HEIGHT - 60;
        g2d.setColor(new Color(200, 180, 160));
        g2d.drawLine(PADDING, currentY, IMAGE_WIDTH - PADDING, currentY);
        
        // 绘制底部文字
        Font footerFont = new Font("SimSun", Font.ITALIC, 20);
        g2d.setFont(footerFont);
        g2d.setColor(new Color(150, 150, 150));
        String footer = "AI工具箱 - 运势测试";
        FontMetrics footerFm = g2d.getFontMetrics();
        int footerWidth = footerFm.stringWidth(footer);
        int footerX = (IMAGE_WIDTH - footerWidth) / 2;
        g2d.drawString(footer, footerX, IMAGE_HEIGHT - 20);
        
        g2d.dispose();
        
        log.info("运势图片生成成功: name={}, birthDate={}", name, birthDate);
        return image;
    }

    /**
     * 文本行数据类
     */
    private static class TextLine {
        String text;
        boolean isSection; // 是否为小标题
        
        TextLine(String text, boolean isSection) {
            this.text = text;
            this.isSection = isSection;
        }
    }
    
    /**
     * 处理运势文本，识别小标题和内容
     */
    private List<TextLine> processFortuneText(String text, int maxWidth, Graphics2D g2d, 
                                              Font contentFont, Font sectionFont) {
        List<TextLine> lines = new ArrayList<>();
        FontMetrics contentFm = g2d.getFontMetrics(contentFont);
        
        // 按行分割
        String[] rawLines = text.split("\n");
        
        for (String rawLine : rawLines) {
            rawLine = rawLine.trim();
            if (rawLine.isEmpty()) {
                continue;
            }
            
            // 移除Markdown格式符号（如果还有残留）
            rawLine = rawLine.replaceAll("^#+\\s*", "")
                             .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                             .replaceAll("\\*([^*]+)\\*", "$1")
                             .replaceAll("-{3,}", "")
                             .replaceAll("^\\s*\\*\\s+", "")
                             .replaceAll("^\\s*-\\s+", "")
                             .trim();
            
            if (rawLine.isEmpty()) {
                continue;
            }
            
            // 判断是否为小标题（如：今日运势、本周运势、本月运势）
            boolean isSection = rawLine.matches(".*运势$") || 
                               rawLine.matches(".*运势.*") ||
                               rawLine.matches("^[事业爱情健康财运]：?$");
            
            if (isSection) {
                // 清理冒号后的内容，只保留标题部分
                if (rawLine.contains("：")) {
                    String[] parts = rawLine.split("：", 2);
                    if (parts.length > 0) {
                        rawLine = parts[0].trim();
                    }
                }
                // 移除emoji和特殊符号
                rawLine = rawLine.replaceAll("[🌟📅📆✨]", "").trim();
                if (!rawLine.isEmpty()) {
                    lines.add(new TextLine(rawLine, true));
                }
            } else {
                // 处理内容行，可能需要换行
                // 如果行以"事业："、"爱情："等开头，提取标签和内容
                if (rawLine.matches("^[事业爱情健康财运]：.*")) {
                    String[] parts = rawLine.split("：", 2);
                    if (parts.length == 2) {
                        String label = parts[0].trim();
                        String content = parts[1].trim();
                        // 添加标签行
                        lines.add(new TextLine(label + "：", false));
                        // 处理内容
                        List<String> wrappedLines = wrapLine(content, maxWidth, contentFm);
                        for (String wrappedLine : wrappedLines) {
                            lines.add(new TextLine("  " + wrappedLine, false)); // 添加缩进
                        }
                    } else {
                        List<String> wrappedLines = wrapLine(rawLine, maxWidth, contentFm);
                        for (String wrappedLine : wrappedLines) {
                            lines.add(new TextLine(wrappedLine, false));
                        }
                    }
                } else {
                    List<String> wrappedLines = wrapLine(rawLine, maxWidth, contentFm);
                    for (String wrappedLine : wrappedLines) {
                        lines.add(new TextLine(wrappedLine, false));
                    }
                }
            }
        }
        
        return lines;
    }
    
    /**
     * 单行文本换行处理
     */
    private List<String> wrapLine(String line, int maxWidth, FontMetrics fm) {
        List<String> result = new ArrayList<>();
        
        // 如果行长度不超过最大宽度，直接返回
        if (fm.stringWidth(line) <= maxWidth) {
            result.add(line);
            return result;
        }
        
        // 需要换行
        StringBuilder currentLine = new StringBuilder();
        for (char c : line.toCharArray()) {
            String testLine = currentLine.toString() + c;
            int testWidth = fm.stringWidth(testLine);
            if (testWidth > maxWidth && currentLine.length() > 0) {
                result.add(currentLine.toString());
                currentLine = new StringBuilder(String.valueOf(c));
            } else {
                currentLine.append(c);
            }
        }
        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }
        
        return result;
    }

    /**
     * 生成星座运势图片
     * 
     * @param constellation 星座名称
     * @param fortuneText 运势文案
     * @return 运势图片
     */
    @Override
    public BufferedImage generateConstellationFortuneImage(String constellation, String fortuneText) {
        // 创建图片
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置背景渐变
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 250, 240),
                0, IMAGE_HEIGHT, new Color(255, 245, 230)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        int currentY = PADDING;
        
        // 绘制标题
        Font titleFont = new Font("SimHei", Font.BOLD, TITLE_FONT_SIZE);
        g2d.setFont(titleFont);
        g2d.setColor(new Color(139, 69, 19)); // 棕色
        
        String title = constellation + " 今日运势";
        FontMetrics titleFm = g2d.getFontMetrics();
        int titleWidth = titleFm.stringWidth(title);
        int titleX = (IMAGE_WIDTH - titleWidth) / 2;
        g2d.drawString(title, titleX, currentY + titleFm.getAscent());
        currentY += titleFm.getHeight() + 20;
        
        // 绘制日期
        Font dateFont = new Font("SimSun", Font.PLAIN, 24);
        g2d.setFont(dateFont);
        g2d.setColor(new Color(128, 128, 128));
        String dateText = "日期：" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        FontMetrics dateFm = g2d.getFontMetrics();
        int dateWidth = dateFm.stringWidth(dateText);
        int dateX = (IMAGE_WIDTH - dateWidth) / 2;
        g2d.drawString(dateText, dateX, currentY + dateFm.getAscent());
        currentY += dateFm.getHeight() + 40;
        
        // 绘制分割线
        g2d.setColor(new Color(200, 180, 160));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(PADDING, currentY, IMAGE_WIDTH - PADDING, currentY);
        currentY += 30;
        
        // 绘制运势内容
        Font contentFont = new Font("SimSun", Font.PLAIN, CONTENT_FONT_SIZE);
        Font sectionFont = new Font("SimHei", Font.BOLD, SECTION_FONT_SIZE);
        g2d.setFont(contentFont);
        g2d.setColor(new Color(60, 60, 60));
        
        // 处理文本换行和格式
        List<TextLine> lines = processFortuneText(fortuneText, IMAGE_WIDTH - 2 * PADDING, g2d, contentFont, sectionFont);
        
        FontMetrics contentFm = g2d.getFontMetrics(contentFont);
        FontMetrics sectionFm = g2d.getFontMetrics(sectionFont);
        int contentLineHeight = contentFm.getHeight() + LINE_SPACING;
        int sectionLineHeight = sectionFm.getHeight() + LINE_SPACING + 10;
        
        for (TextLine line : lines) {
            int lineHeight = line.isSection ? sectionLineHeight : contentLineHeight;
            
            // 检查是否超出图片范围
            if (currentY + lineHeight > IMAGE_HEIGHT - 80) {
                log.warn("内容超出图片范围，已截断");
                break;
            }
            
            // 设置字体和颜色
            if (line.isSection) {
                g2d.setFont(sectionFont);
                g2d.setColor(new Color(139, 69, 19)); // 棕色
            } else {
                g2d.setFont(contentFont);
                g2d.setColor(new Color(60, 60, 60)); // 深灰色
            }
            
            FontMetrics fm = line.isSection ? sectionFm : contentFm;
            g2d.drawString(line.text, PADDING, currentY + fm.getAscent());
            currentY += lineHeight;
        }
        
        // 绘制底部装饰
        currentY = IMAGE_HEIGHT - 60;
        g2d.setColor(new Color(200, 180, 160));
        g2d.drawLine(PADDING, currentY, IMAGE_WIDTH - PADDING, currentY);
        
        // 绘制底部文字
        Font footerFont = new Font("SimSun", Font.ITALIC, 20);
        g2d.setFont(footerFont);
        g2d.setColor(new Color(150, 150, 150));
        String footer = "AI工具箱 - 星座运势";
        FontMetrics footerFm = g2d.getFontMetrics();
        int footerWidth = footerFm.stringWidth(footer);
        int footerX = (IMAGE_WIDTH - footerWidth) / 2;
        g2d.drawString(footer, footerX, IMAGE_HEIGHT - 20);
        
        g2d.dispose();
        
        log.info("星座运势图片生成成功: constellation={}", constellation);
        return image;
    }
}

