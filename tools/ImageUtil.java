package tools;

import cn.magicwindow.score.common.bean.TextLine;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import sun.font.FontDesignMetrics;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * @author
 * @package
 * @class ImageUtil
 * @date 2019/09/19 11:29
 * @description
 */
@Slf4j
public class ImageUtil {

    public static final String IMAGE_PREFIX = "data:image/png;base64,";

    public static BufferedImage watermark(BufferedImage source, String overUrl, int x, int y, float alpha,
                                          int waterWidth, int waterHeight) {
        try {
            // 获取底图
            BufferedImage buffImg = ImageIO.read(new URL(overUrl));
            // 圆角处理
            buffImg = makeRoundedCorner(buffImg, 600);

            drawImageGraphics(buffImg, x, y, alpha, source, waterWidth, waterHeight);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return source;

    }

    private static void drawImageGraphics(BufferedImage waterImg, int x, int y, float alpha, BufferedImage buffImg,
                                          int waterWidth, int waterHeight) {
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();

        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制
        g2d.drawImage(waterImg, x, y, waterWidth, waterHeight, null);
        // 释放图形上下文使用的系统资源
        g2d.dispose();
    }

    public static BufferedImage drawStringGraphics(String nickName, float alpha,
                                                   BufferedImage buffImg, String fontStyle, int size, int fontBold, Color color, int startX, int endY) {
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();

        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font(fontStyle, fontBold, size);

        g2d.setFont(font);
        g2d.setColor(Color.black);

        g2d.setFont(font);
        g2d.setColor(color);
        g2d.drawString(nickName, startX, endY);

        // 释放图形上下文使用的系统资源
        g2d.dispose();
        return buffImg;
    }

    public static BufferedImage drawCenterStringGraphics(String nickName, float alpha,
                                                         BufferedImage buffImg, String fontStyle, int size, int fontBold, Color color, int endY) {
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();

        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font(fontStyle, fontBold, size);

        g2d.setFont(font);
        g2d.setColor(Color.black);

        g2d.setFont(font);
        g2d.setColor(color);

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 计算文字长度，计算居中的x点坐标
        FontMetrics fm = g2d.getFontMetrics(font);
        int textWidth = fm.stringWidth(nickName);
        int widthX = (buffImg.getWidth() - textWidth) / 2;
        g2d.drawString(nickName, widthX, endY);
        // 释放图形上下文使用的系统资源
        g2d.dispose();
        return buffImg;
    }

    /**
     * 生成圆角图片
     *
     * @param image        原始图片
     * @param cornerRadius 圆角的弧度
     * @return 返回圆角图
     */
    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    /**
     * bufferedImage转Byte
     *
     * @param bufferedImage
     * @param formatName
     * @return
     * @throws IOException
     */
    public static byte[] bufferedImageToByte(BufferedImage bufferedImage, String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageIO.write(bufferedImage, formatName, baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        return imageInByte;
    }

    /**
     * 按照 宽高 比例压缩
     *
     * @param scale 压缩刻度
     * @return 压缩后图片数据
     * @throws IOException 压缩图片过程中出错
     */
    public static byte[] compress(byte[] srcImgData, double scale) throws IOException {
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(srcImgData));

        // 源图宽度
        int width = (int) (bi.getWidth() * scale);
        // 源图高度
        int height = (int) (bi.getHeight() * scale);

        Image image = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics g = tag.getGraphics();
        g.setColor(Color.RED);
        // 绘制处理后的图
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ImageIO.write(tag, "PNG", bOut);

        return bOut.toByteArray();
    }

    /**
     * 按照 宽高 比例压缩
     *
     * @param scale 压缩刻度
     * @return 压缩后图片数据
     * @throws IOException 压缩图片过程中出错
     */
    public static byte[] compress(byte[] srcImgData, double scale, String imageType) throws IOException {
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(srcImgData));

        // 源图宽度
        int width = (int) (bi.getWidth() * scale);
        // 源图高度
        int height = (int) (bi.getHeight() * scale);

        Image image = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics g = tag.getGraphics();
        g.setColor(Color.RED);
        // 绘制处理后的图
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ImageIO.write(tag, imageType, bOut);

        return bOut.toByteArray();
    }

    public static byte[] zoomPictures(BufferedImage bufferedImage) throws IOException {
        double scale = 0;
        if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
            scale = new BigDecimal("200").divide(new BigDecimal(String.valueOf(bufferedImage.getWidth())), 1, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        } else {
            scale = new BigDecimal("200").divide(new BigDecimal(String.valueOf(bufferedImage.getHeight())), 1, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        }

        byte[] datas = ImageUtil.compress(bufferedImageToByte(bufferedImage, "jpg"), scale, "JPEG");

        return datas;
    }

    /**
     * 加载图片并缩放
     *
     * @param imageUrl 网络图片地址
     * @param width    缩放宽度
     * @param height   缩放高度
     * @return
     */
    public static BufferedImage loadAndZoomPic(String imageUrl, int width, int height) {
        try {
            BufferedImage src = ImageIO.read(new URL(imageUrl));
            //获取图片原始宽和高
            int srcWidth = src.getWidth();
            int srcHeight = src.getHeight();

            if (width == 0 && height > 0) {
                width = (int) (srcWidth * 1.0 * height / srcHeight);
            } else if (width > 0 && height == 0) {
                height = (int) (srcHeight * 1.0 * width / srcWidth);
            }
            return zoomImage(src, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 图片缩放
     *
     * @param bufImg
     * @param w      缩放的目标宽度
     * @param h      缩放的目标高度
     * @return
     * @throws Exception
     */
    public static BufferedImage zoomImage(BufferedImage bufImg, int w, int h) throws Exception {
        double wr = 0, hr = 0;
        //获取缩放比例
        wr = w * 1.0 / bufImg.getWidth();
        hr = h * 1.0 / bufImg.getHeight();
        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        try {
            return ato.filter(bufImg, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 绘制背景颜色
     *
     * @param w 目标宽度
     * @param h 目标高度
     * @return
     * @throws Exception
     */
    public static BufferedImage drawBackground(int w, int h, Color color) throws Exception {
        //背景图
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D dg = bufferedImage.createGraphics();
        dg.setColor(color);
        dg.fillRect(0, 0, w, h);
        dg.dispose();

        return bufferedImage;
    }

    /**
     * 获取单个字体的宽度
     *
     * @param font 字体样式
     * @param word 字体
     * @return
     */
    public static int getSingleWordWidth(Font font, char word) {
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        return metrics.charWidth(word);
    }

    /**
     * 绘制文字，文字定位坐标起始位置：左下角
     *
     * @param lineText 内容
     * @param alpha    透明度
     * @param buffImg
     * @param font     字体样式
     * @param color    字体颜色
     * @param startX   x坐标
     * @param endY     y坐标
     * @return
     */
    public static BufferedImage drawTextGraphics(String lineText, float alpha,
                                                 BufferedImage buffImg, Font font, Color color, int startX, int endY) {
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.black);
        g2d.setFont(font);
        g2d.setColor(color);
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawString(lineText, startX, endY);
        // 释放图形上下文使用的系统资源
        g2d.dispose();
        return buffImg;
    }

    /**
     * 内容换行
     *
     * @param text  内容
     * @param width 换行宽度
     * @param font  文字样式
     * @return
     */
    public static List<TextLine> split2lines(String text, int width, Font font) {
        List<TextLine> rows = new ArrayList<>();

        text = text.replaceAll("\\n", "\n").trim();

        int currentWidth = 0;
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                rows.add(new TextLine(text.substring(start, i), currentWidth));
                start = i;
                currentWidth = 0;
            } else {
                int charWidth = ImageUtil.getSingleWordWidth(font, text.charAt(i));
                if (currentWidth + charWidth <= width) {
                    currentWidth += charWidth;
                } else {
                    rows.add(new TextLine(text.substring(start, i), currentWidth));
                    start = i;
                    currentWidth = charWidth;
                }
            }
        }
        if (start < text.length()) {
            rows.add(new TextLine(text.substring(start), currentWidth));
        }
        return rows;

    }

    /**
     * 绘制图片，图片定位坐标起始位置：左上角
     *
     * @param source 底图
     * @param over   目标图
     * @param x      x坐标
     * @param y      y坐标
     * @param alpha  透明度
     * @return
     * @throws IOException
     */
    public static BufferedImage watermark(BufferedImage source, BufferedImage over, int x, int y, float alpha) throws IOException {
        drawImageGraphics(over, x, y, alpha, source);
        return source;
    }

    /**
     * 绘制图片，图片定位坐标起始位置：左上角
     *
     * @param waterImg 目标图
     * @param x        x坐标
     * @param y        y坐标
     * @param alpha    透明度
     * @param buffImg  底图
     */
    private static void drawImageGraphics(BufferedImage waterImg, int x, int y, float alpha, BufferedImage buffImg) {
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        // 获取层图的高度
        int waterImgHeight = waterImg.getHeight();
        // 获取层图的宽度
        int waterImgWidth = waterImg.getWidth();
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
        // 释放图形上下文使用的系统资源
        g2d.dispose();
    }

    /**
     * 引入自定义的字体
     *
     * @param fontsPath 字体路径
     * @param fontStyle 字体样式
     * @param fontSize  字体大小
     * @return
     */
    public static Font customizeFont(String fontsPath, int fontStyle, float fontSize) {
        Font font = null;
        InputStream inputStream = null;
        try {
            Resource resource = new ClassPathResource(fontsPath);
            inputStream = resource.getInputStream();
            Font tempFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            font = tempFont.deriveFont(fontSize);
            font = font.deriveFont(fontStyle);
            GraphicsEnvironment ge = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return font;
    }

    /**
     * 绘制矩形
     *
     * @param buffImg
     * @param borderColor 边框颜色
     * @param background  背景严责
     * @param startX      起始位置
     * @param starY
     * @param width       宽
     * @param height      高
     * @param alpha       透明度
     * @return
     */
    public static BufferedImage drawRectangleGraphics(BufferedImage buffImg, Color borderColor, Color background, int startX, int starY, int width, int height, float alpha, float borderHeight) {
        Graphics2D g2d = buffImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(borderColor);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                alpha));
        // 1. 绘制一个矩形空心: 起点(30, 20), 宽80, 高100
        g2d.drawRect(startX, starY, width, height);

        // 2. 填充一个矩形实心
        if (Preconditions.isNotBlank(background)) {
            g2d.setColor(background);
            g2d.fillRect(startX, starY, width, height);

        }
        if (Preconditions.isNotBlank(borderHeight) && borderHeight > 0f) {
            g2d.setStroke(new BasicStroke(borderHeight));
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g2d.dispose();
        return buffImg;
    }

    /**
     * 绘制圆角矩形
     *
     * @param buffImg
     * @param borderColor
     * @param startX
     * @param starY
     * @param width
     * @param height
     * @param arcWidth
     * @param arcHeight
     * @param borderHeight
     * @return
     */
    public static BufferedImage drawArcRectangleGraphics(BufferedImage buffImg, Color borderColor,Color bgColor ,int startX, int starY, int width, int height, int arcWidth, int arcHeight, float borderHeight) {
        Graphics2D g2d = buffImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(borderColor);

        // 2. 填充一个矩形
//        g2d.fillRect(startX, starY, width, height);

        // 3. 绘制一个圆角矩形: 起点(30, 150), 宽80, 高100, 圆角宽30, 圆角高30
        g2d.drawRoundRect(startX, starY, width, height, arcWidth, arcHeight);
        if (Preconditions.isNotBlank(borderHeight) && borderHeight > 0f) {
            g2d.setStroke(new BasicStroke(borderHeight));
        }

        if (Preconditions.isNotBlank(bgColor)) {
            g2d.setColor(bgColor);
            g2d.fillRoundRect(startX, starY, width, height, arcWidth, arcHeight);
        }

        g2d.dispose();

        return buffImg;
    }


    /**
     * 设置渐变色
     *
     * @param buffImg
     * @param starBackground
     * @param endBackground
     * @param startX
     * @param starY
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage paintGradientPaint(BufferedImage buffImg, Color starBackground, Color endBackground, int startX, int starY, int width, int height) {
        Graphics2D g2d = buffImg.createGraphics();
        Rectangle2D.Float rect = new Rectangle2D.Float(startX, starY, width, height);// 创建矩形对象
        // 创建循环渐变的GraphientPaint对象
        GradientPaint paint = new GradientPaint(startX, starY, starBackground, width, height, endBackground);
        g2d.setPaint(paint);// 设置渐变
        g2d.fillRect(startX, starY, width, height);
        g2d.fill(rect);// 绘制矩形
        g2d.dispose();
        return buffImg;
    }

    /**
     * 渐变色新版本(以后请使用过此版本)
     *
     * @param buffImg
     * @param starBackground
     * @param endBackground
     * @param startX
     * @param starY
     * @param width
     * @param height
     * @param endX
     * @param endY
     * @param cyclic
     * @return
     */
    public static BufferedImage paintGradientPaint2(BufferedImage buffImg, Color starBackground, Color endBackground, int startX, int starY, int width, int height, int endX, int endY, boolean cyclic) {
        Graphics2D g2d = buffImg.createGraphics();
        Rectangle2D.Float rect = new Rectangle2D.Float(startX, starY, width, height);// 创建矩形对象
        // 创建循环渐变的GraphientPaint对象
        GradientPaint paint = new GradientPaint(startX, starY, starBackground, endX, endY, endBackground, cyclic);
        g2d.setPaint(paint);// 设置渐变
        g2d.fillRect(startX, starY, width, height);
        g2d.fill(rect);// 绘制矩形
        g2d.dispose();
        return buffImg;
    }


    /**
     * 绘制圆形头像
     *
     * @param buffImg
     * @param imageUrl     图片网络地址
     * @param startX       起始位置
     * @param starY
     * @param targetWidth  宽
     * @param targetHeight 高
     * @return
     * @throws IOException
     */
    public static BufferedImage drawHeader(BufferedImage buffImg, String imageUrl, int startX, int starY, int targetWidth, int targetHeight) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage srcImage = ImageIO.read(url);
        return drawHeader(buffImg, srcImage, startX, starY, targetWidth, targetHeight);
    }

    /**
     * @param buffImg
     * @param srcImage     图片buff
     * @param startX       起始位置
     * @param starY
     * @param targetWidth  宽
     * @param targetHeight 高
     * @return
     * @throws IOException
     */
    public static BufferedImage drawHeader(BufferedImage buffImg, BufferedImage srcImage, int startX, int starY, int targetWidth, int targetHeight) throws IOException {
        Graphics2D g2 = buffImg.createGraphics();
        TexturePaint texturePaint = new TexturePaint(srcImage, new Rectangle2D.Float(startX, starY, targetWidth, targetHeight));
        g2.setPaint(texturePaint);
        Ellipse2D.Float ellipse = new Ellipse2D.Float(startX, starY, targetWidth, targetHeight);
        // 抗锯齿
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fill(ellipse);
        g2.dispose();
        return buffImg;
    }

    /**
     * 绘制圆形框
     *
     * @param buffImg
     * @param borderColor  颜色
     * @param startX       起始坐标
     * @param starY
     * @param targetWidth  宽
     * @param targetHeight 高
     */
    public static BufferedImage drawArc(BufferedImage buffImg, Color borderColor, int startX, int starY, int targetWidth, int targetHeight, Color fillColor) {
        Graphics2D g2d = buffImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(borderColor);

        // 2. 绘制一个圆: 圆的外切矩形 左上角坐标为(120, 20), 宽高为100
        g2d.drawArc(startX, starY, targetWidth, targetHeight, 0, 360);
        if (Preconditions.isNotBlank(fillColor)) {
            g2d.setColor(fillColor);
            // 3. 填充一个圆形
            g2d.fillArc(startX, starY, targetWidth, targetHeight, 0, 360);
        }
        g2d.dispose();
        return buffImg;
    }

    /**
     * 线段 / 折线
     *
     * @param buffImg
     * @param borderColor  颜色
     * @param startX       起始坐标
     * @param startY
     * @param endX         结束坐标
     * @param endY
     * @param targetHeight 线高
     * @param isLine       是否是实线
     * @return
     */
    public static BufferedImage drawLine(BufferedImage buffImg, Color borderColor, int startX, int startY, int endX, int endY, float targetHeight, boolean isLine) {
        // 创建 Graphics 的副本, 需要改变 Graphics 的参数,
        // 这里必须使用副本, 避免影响到 Graphics 原有的设置
        Graphics2D g2d = buffImg.createGraphics();
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置画笔颜色
        g2d.setColor(borderColor);

        BasicStroke bs2 = null;
        if (isLine) {
            // 3. 实线  两点绘制线段（设置线宽为5px）: 点(50, 150), 点(200, 150)
            // 笔画的轮廓（画笔宽度/线宽为5px）
            bs2 = new BasicStroke(targetHeight);
        } else {
            // 绘制虚线: 将虚线分为若干段（ 实线段 和 空白段 都认为是一段）, 实线段 和 空白段 交替绘制,
            //             绘制的每一段（包括 实线段 和 空白段）的 长度 从 dash 虚线模式数组中取值（从首
            //             元素开始循环取值）, 下面数组即表示每段长度分别为: 5px, 10px, 5px, 10px, ...
            float[] dash = new float[]{5, 10};
            bs2 = new BasicStroke(
                    // 画笔宽度/线宽
                    targetHeight,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    // 虚线模式数组
                    dash,
                    0.0f
            );
        }

        g2d.setStroke(bs2);
        g2d.drawLine(startX, startY, endX, endY);
        // 自己创建的副本用完要销毁掉
        g2d.dispose();
        return buffImg;
    }

    /**
     * 平铺小图图片
     * <p>
     * bufferImage      画布
     * tiledBufferImage 平铺的小图
     *
     * @param margin_x  水印之间的水平间距
     * @param margin_y  水印之间的垂直间距
     * @param opacity   水印透明度
     * @param markAngle 水印旋转角度
     * @throws IOException
     */
    public static BufferedImage markBackgroundImage(BufferedImage bufferImage, BufferedImage tiledBufferImage,
                                                    int margin_x, int margin_y, float opacity, double markAngle) throws IOException {

        Graphics2D graphics = (Graphics2D) bufferImage.getGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
        graphics.rotate(markAngle);

        int canvasHeight = bufferImage.getHeight();
        int canvasWidth = bufferImage.getWidth();
        int markHeight = tiledBufferImage.getHeight();
        int markWidth = tiledBufferImage.getHeight();
        for (int i = -canvasHeight; i < canvasWidth + canvasHeight; i = i + markWidth + margin_x) {
            for (int j = -canvasWidth; j < canvasHeight + canvasWidth; j = j + markHeight + margin_y) {
                graphics.drawImage(tiledBufferImage, i, j, tiledBufferImage.getWidth(), tiledBufferImage.getHeight(), null);
            }
        }
        graphics.dispose();
        return bufferImage;
    }

    /**
     * 基础绘图
     *
     * @param bufferedImage 画布
     * @param imageUrl      图片地址
     * @param width         宽
     * @param height        高
     * @param x             起点x坐标
     * @param y             起点y坐标
     * @return
     * @throws Exception
     */
    public static BufferedImage drawPic(BufferedImage bufferedImage, String imageUrl, int width, int height, int x, int y) throws Exception {
        BufferedImage src = ImageIO.read(new URL(imageUrl));
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        if (width == 0 && height > 0) {
            width = (int) (srcWidth * 1.0 * height / srcHeight);
        } else if (width > 0 && height == 0) {
            height = (int) (srcHeight * 1.0 * width / srcWidth);
        }

        bufferedImage = watermark(bufferedImage, zoomImage(src, width, height), x, y, 1.0f);
        return bufferedImage;
    }

    /**
     * @param bufferedImage       画布
     * @param targetBufferedImage 绘制的图片
     * @param width
     * @param height
     * @param x
     * @param y
     * @return
     * @throws Exception
     */
    public static BufferedImage drawPic(BufferedImage bufferedImage, BufferedImage targetBufferedImage, int width, int height, int x, int y) throws Exception {

        int srcHeight = targetBufferedImage.getHeight();
        int srcWidth = targetBufferedImage.getWidth();

        if (width == 0 && height > 0) {
            width = (int) (srcWidth * 1.0 * height / srcHeight);
        } else if (width > 0 && height == 0) {
            height = (int) (srcHeight * 1.0 * width / srcWidth);
        }

        bufferedImage = watermark(bufferedImage, zoomImage(targetBufferedImage, width, height), x, y, 1.0f);
        return bufferedImage;
    }


    /**
     * 图片转base64
     *
     * @param bufferedImage
     * @return
     */
    public static String bufferImage2Base64(BufferedImage bufferedImage) {
        byte[] bytes = new byte[0];
        try {
            bytes = ImageUtil.bufferedImageToByte(bufferedImage, "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] compressBytes = new byte[0];
        try {
            compressBytes = ImageUtil.compress(bytes, 1, "JPEG");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        //转换成base64串
        String base64 = encoder.encodeBuffer(compressBytes).trim();
        //删除 \r\n
        base64 = base64.replaceAll("\n", "").replaceAll("\r", "");
        return base64;

    }

    /**
     * base64字符串转化成图片
     *
     * @param base64String
     * @return
     */
    public static BufferedImage base64String2BufferImage(String base64String) {
        //对字节数组字符串进行Base64解码并生成图片
        if (base64String == null) {
            //图像数据为空
            return null;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BufferedImage bi = ImageIO.read(bais);
            return bi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 高斯模糊执行方法
     *
     * @param img    原图片
     * @param radius 模糊权重
     * @return 模糊后图片
     */
    public static BufferedImage gaussianBlur(BufferedImage img, int radius) throws IOException {
        int height = img.getHeight();
        int width = img.getWidth();
        int[] values = getPixArray(img, width, height);
        values = doBlur(values, width, height, radius);
        img.setRGB(0, 0, width, height, values, 0, width);
        return img;
    }

    /**
     * 获取图像像素矩阵
     *
     * @param im
     * @param w
     * @param h
     * @return
     */
    private static int[] getPixArray(Image im, int w, int h) {
        int[] pix = new int[w * h];
        PixelGrabber pg = null;
        try {
            pg = new PixelGrabber(im, 0, 0, w, h, pix, 0, w);
            if (pg.grabPixels() != true) {
                try {
                    throw new AWTException("pg error" + pg.status());
                } catch (Exception eq) {
                    eq.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return pix;
    }

    /**
     * 高斯模糊算法。
     *
     * @param pix
     * @param w
     * @param h
     * @param radius
     * @return
     */
    public static int[] doBlur(int[] pix, int w, int h, int radius) throws IOException {
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;
            }
        }
        return pix;
    }

    /**
     * 压缩文件到30K
     *
     * @param src
     * @return
     */
    public static BufferedImage resizeImageTo30K(BufferedImage src) {
        try {
//            src = cropImage(src, 0, 0, 200, 200);
            BufferedImage output = Thumbnails.of(src).size(src.getWidth(), src.getHeight()).asBufferedImage();
            String base64 = bufferImage2Base64(output);
            int i = base64.length() - base64.length() / 8 * 2;
            while (i > 30720) {
                log.info("文件大小：" + i);
                output = Thumbnails.of(output).scale(30720f / i).asBufferedImage();
                base64 = bufferImage2Base64(output);
                i = base64.length() - base64.length() / 8 * 2;
            }
            log.info("last文件大小：" + i);
            return output;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 下载文件到本地
     *
     * @param urlString 网络地址
     * @param filename  文件名
     * @param savePath  保存地址
     * @throws Exception
     */
    public static void downloadNetPic(String urlString, String filename, String savePath) throws Exception {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5 * 1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        OutputStream os = new FileOutputStream(sf.getPath() + "/" + filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }

    /**
     * 字节数组转BufferedImage
     *
     * @param b
     * @return
     */
    public static BufferedImage byteArray2BufferedImage(byte[] b) {
        try {
            //将b作为输入流；
            ByteArrayInputStream in = new ByteArrayInputStream(b);
            //将in作为输入流，读取图片存入image中，而这里in可以为ByteArrayInputStream();
            BufferedImage image = ImageIO.read(in);
            return image;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * 裁剪图片方法
     *
     * @param bufferedImage 图像源
     * @param startX        裁剪开始x坐标
     * @param startY        裁剪开始y坐标
     * @param endX          裁剪结束x坐标
     * @param endY          裁剪结束y坐标
     * @return
     */
    public static BufferedImage cropImage(BufferedImage bufferedImage, int startX, int startY, int endX, int endY) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (startX == -1) {
            startX = 0;
        }
        if (startY == -1) {
            startY = 0;
        }
        if (endX == -1) {
            endX = width - 1;
        }
        if (endY == -1) {
            endY = height - 1;
        }
        BufferedImage result = new BufferedImage(endX - startX, endY - startY, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        for (int x = startX; x < endX; ++x) {
            for (int y = startY; y < endY; ++y) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(x - startX, y - startY, rgb);
            }
        }
        return result;
    }

    public static byte[] downloadPic(String url) throws Exception {
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            //通过输入流获取图片数据
            InputStream inStream = conn.getInputStream();
            //得到图片的二进制数据
            byte[] btImg = readInputStream(inStream);
            return btImg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        //将这个图片拷贝到你项目根目录下
        String imageUrl = "https://img.liaoyantech.cn/FrOHrcTcyVq_qUiPpRvz3Jdzpsgj";
        BufferedImage bufferedImage = ImageIO.read(new URL(imageUrl));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int startx = 0;
        int startY = 0;
        int endx = 0;
        int endY = 0;
        if (width > height)  {
            startx = width/2 - height/2;
            startY = height/2 - height/2;
            endx = width/2 + height/2;
            endY = height/2 + height/2;
        } else {
            startx = width/2 - width/2;
            startY = height/2 - width/2;
            endx = width/2 + width/2;
            endY = height/2 + width/2;
        }

        bufferedImage = ImageUtil.cropImage(bufferedImage, startx, startY, endx, endY);
//        BufferedImage bufferedImage = ImageUtil.drawBackground(750, 750, Color.BLACK);
//        ImageUtil.paintGradientPaint2(bufferedImage, new Color(255, 255, 255, 0), new Color(255, 255, 255), 0, 100, 750, 204, 0, 300, false);
        File outputfile = new File("/Users/leon/pic/test.png");
        ImageIO.write(bufferedImage, "png", outputfile);
    }

}
