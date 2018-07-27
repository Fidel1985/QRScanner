package com.fidel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.decoder.Decoder;
import com.google.zxing.datamatrix.detector.Detector;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.EnumMap;
import java.util.Map;

public class Main {

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
                    Map<DecodeHintType, Object> hintsMap = new EnumMap<>(DecodeHintType.class);
                    hintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                    hintsMap.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.DATA_MATRIX);
                    Detector detector = new Detector(binaryBitmap.getBlackMatrix());

                    Decoder decoder = new Decoder();
                    DetectorResult detectorResult = detector.detect();
                    DecoderResult result = decoder.decode(detectorResult.getBits());
                    System.out.println(result.getText());
                //} catch (NotFoundException ex) {
                } catch (NotFoundException | FormatException | ChecksumException ex) {
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
                } catch (IOException ignored) {
                }
            }
        }

    }

}