package logic;

import org.apache.lucene.benchmark.utils.ExtractReuters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleReader extends ExtractReuters {
    private final Pattern EXTRACTION_PATTERN = Pattern.compile("<PLACES><D>(.+?)</D></PLACES>|<BODY>(.+?)</BODY>");
    private static final String[] META_CHARS = new String[]{"&", "<", ">", "\"", "'", "", ";"};
    private static final String[] META_CHARS_SERIALIZATIONS = new String[]{"&amp;", "&lt;", "&gt;", "&quot;", "&apos;", "<D>", "</D>"};
    private static final HashSet<String> COUNTRY_FILTER_VALUES = new HashSet<String>(Arrays.asList("west-germany", "usa", "france", "uk", "canada", "japan"));
    final private Path inputDir;
    final private Path outputDir;
    private static final String SEMICOLON_CHAR = ";";

    public ArticleReader(Path inputDir, Path outputDir) throws IOException {
        super(inputDir, outputDir);
        this.inputDir = inputDir;
        this.outputDir = outputDir;
    }

    public static void checkIfDirExists(Path path) {
        File directory = new File(path.toString());
        if (!directory.exists()){
            directory.mkdir();
        }
    }

    @Override
    public void extract() {
        try {
            System.out.println("Extraction in progress...");
            super.extract();
        } catch (IOException ex) {
            ex.printStackTrace();
            ex.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        System.out.println("Extraction finished!");
    }

    @Override
    protected void extractFile(Path sgmFile) {
        try {
            BufferedReader reader = Files.newBufferedReader(sgmFile, StandardCharsets.ISO_8859_1);
            Throwable var3 = null;

            try {
                StringBuilder buffer = new StringBuilder(1024);
                StringBuilder outBuffer = new StringBuilder(1024);
                String line = null;
                int var7 = 0;

                while(true) {
                    while((line = reader.readLine()) != null) {
                        if (line.indexOf("</REUTERS") == -1) {
                            buffer.append(line).append(' ');
                        } else {
                            Matcher matcher = this.EXTRACTION_PATTERN.matcher(buffer);

                            while(matcher.find()) {
                                for(int i = 1; i <= matcher.groupCount(); ++i) {
                                    if (matcher.group(i) != null) {
                                        outBuffer.append(matcher.group(i));
                                    }
                                }

                                outBuffer.append(System.lineSeparator()).append(System.lineSeparator());
                            }

                            String out = outBuffer.toString();

                            for(int i = 0; i < META_CHARS_SERIALIZATIONS.length; ++i) {
                                out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
                            }

                            String[] text = out.split(System.lineSeparator(), 2);
                            String body = null;
                            String[] countryArray = null;
                            if (text.length > 1) {
                                countryArray = text[0].trim().split(SEMICOLON_CHAR);
                                body = text[1].trim();
                            }
                            BufferedWriter writer = null;
                            if (body != null && !body.isBlank() && countryArray.length > 0) {
                                boolean isCountryAllowed = false;
                                for (String ctr : countryArray) {
                                    isCountryAllowed = false;
                                    if (COUNTRY_FILTER_VALUES.contains(ctr)) {
                                        isCountryAllowed = true;
                                    } else {
                                        break;
                                    }
                                }
                                if (isCountryAllowed) {
                                    Path outFile = this.outputDir.resolve(sgmFile.getFileName() + "-" + var7++ + ".txt");
                                    writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8);
                                }
                            }
                            Throwable var12 = null;

                            try {
                                if (writer != null) writer.write(out);
                            } catch (Throwable var37) {
                                var12 = var37;
                                throw var37;
                            } finally {
                                if (writer != null) {
                                    if (var12 != null) {
                                        try {
                                            writer.close();
                                        } catch (Throwable var36) {
                                            var12.addSuppressed(var36);
                                        }
                                    } else {
                                        writer.close();
                                    }
                                }

                            }

                            outBuffer.setLength(0);
                            buffer.setLength(0);
                        }
                    }

                    return;
                }
            } catch (Throwable var39) {
                var3 = var39;
                throw var39;
            } finally {
                if (reader != null) {
                    if (var3 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var35) {
                            var3.addSuppressed(var35);
                        }
                    } else {
                        reader.close();
                    }
                }

            }
        } catch (IOException var41) {
            throw new RuntimeException(var41);
        }
    }
}
