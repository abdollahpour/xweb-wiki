package ir.xweb.module;

import info.bliki.wiki.model.WikiModel;
import ir.xweb.util.Tools;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WikiModule extends Module {

    private final static String DEFAULT_WIKI_DIR   = "wiki";

    private final static String FILE_NAME_STRIPPER_REGEX = "^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*";

    public WikiModule(Manager manager, ModuleInfo info, ModuleParam properties) {
        super(manager, info, properties);
    }

    @Override
    public void process(ServletContext context, HttpServletRequest request,
                        HttpServletResponse response, ModuleParam params,
                        HashMap<String, FileItem> files) throws IOException {

        ResourceModule rm = getManager().getModule(ResourceModule.class);

        if(params.containsKey("image")) {
            String path = params.getString("image", null);
            File imageFile = rm.getFile(DEFAULT_WIKI_DIR + File.separator + path);
            rm.writeFile(response, imageFile);
        } else if(params.containsKey("html")) {
            String path = params.getString("html", null);

            path = path.replaceAll("[\\s]", "_");

            File wikiFile = rm.getFile(DEFAULT_WIKI_DIR + File.separator + path + ".txt");

            if(wikiFile != null) {
                File file;

                response.setContentType("text/html");
                response.setCharacterEncoding("utf-8");

                String acceptEncoding = request.getHeader("Accept-Encoding");
                if(acceptEncoding != null && acceptEncoding.toLowerCase().indexOf("gzip") > -1) {
                    response.setHeader("Content-Encoding", "gzip");

                    file = new File(wikiFile.getPath() + ".html.gz");
                } else {
                    file = new File(wikiFile.getPath() + ".html");
                }

                // update file
                if(!file.exists() || file.lastModified() < wikiFile.lastModified()) {
                    convertToWiki(wikiFile, file, request.getContextPath());
                }

                rm.writeFile(response, file);
            } else {
                throw new IllegalArgumentException("File not found: " + path);
            }
        } else if(params.containsKey("clear_cache")) {
            File wikiDir = rm.getFile(DEFAULT_WIKI_DIR);

            List<File> htmls = list(wikiDir, ".html");
            //System.out.println(htmls.size());

            if(htmls != null) {
                for(File html:htmls) {
                    html.delete();
                }
            }
        } else {
            throw new IllegalArgumentException("Illegal request!");
        }

    }

    private List<File> list(File file, String ext) {
        ArrayList<File> files = new ArrayList<File>();
        _list(file, files, ext);
        return files;
    }

    private void _list(File file, ArrayList<File> files, String ext) {
        File[] _files = file.listFiles();
        if(files != null) {
            for(File f:_files) {
                if(f.isDirectory()) {
                    _list(f, files, ext);
                } else if(f.getName().endsWith(ext)) {
                    files.add(f);
                }
            }
        }
    }

    private void convertToWiki(File wikiFile, File file, String path) throws IOException {
        File htmlFile, zipFile;
        if(file.getPath().endsWith(".gz")) {
            htmlFile = new File(file.getPath().substring(0, file.getPath().length() - 3));
            zipFile = file;
        } else {
            htmlFile = file;
            zipFile = new File(file.getPath() + ".gz");
        }


        String wiki = Tools.readTextFile(wikiFile);

        String language = htmlFile.getParentFile().getName();
        WikiModel model = new WikiModel(path + "/wiki-image/" + language + "/${image}", "${title}");

        String html = model.render(wiki, false);
        Tools.writeTextFile(html, htmlFile);
        Tools.gzipFile(htmlFile, zipFile);
    }

}
