package com.fidel;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.detector.MultiDetector;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        PDDocument document = null;
        try (InputStream is = Main.class.getResourceAsStream("/10.pdf")) {

            document = PDDocument.load(is);
            for (PDPage page : document.getPages()) {
                page.setCropBox(new PDRectangle(1817, 17, 17, 17));
            }
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCounter = 0;
            for (PDPage page : document.getPages())
            {
                // note that the page number parameter is zero based
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCounter, 500, ImageType.RGB);
                // suffix in filename will be used as the file format
                ImageIOUtil.writeImage(bim, "10-" + (pageCounter++) + ".png", 500);
                LuminanceSource source = new BufferedImageLuminanceSource(bim);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));


                try {
                    //BitMatrix bitMatrix = binaryBitmap.getBlackMatrix();
                    //MultiDetector detector = new MultiDetector(bitMatrix);
                    //DetectorResult detectorResult = detector.detect();

                    Result result = new MultiFormatReader().decode(binaryBitmap);
                    //QRCodeReader qrCodeReader = new QRCodeReader();
                    //Result result = qrCodeReader.decode(bitmap);
                    System.out.println(result.getText());
                } catch (NotFoundException ex) {
                //} catch (NotFoundException | FormatException ex) {
                    System.out.println("Can't decode page: " + pageCounter);
                }

            }

            document.save("Output.pdf");
            document.close();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Oh, god...
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                }
            }
        }

    }

}