/**
 * XWeb project
 * https://github.com/abdollahpour/xweb
 * Hamed Abdollahpour - 2013
 */

package ir.xweb.module;

import info.bliki.wiki.model.WikiModel;
import ir.xweb.server.Constants;
import ir.xweb.util.Tools;
import org.apache.commons.fileupload.FileItem;
import org.markdown4j.Markdown4jProcessor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;

public class WikiModule extends Module {

    private final static String WIKI_DIR   = "dir.wiki";

    private final static String WIKI_CACHE_DIR   = "dir.cache";

    private final static String FILE_NAME_STRIPPER_REGEX = "^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*";

    private final static String IMAGE_REGEX = "^.+?(?i)(jpg|jpeg|git|png|svg)$";

    private final File wikiDir;

    private File cacheDir;

    public WikiModule(
            final Manager manager,
            final ModuleInfo info,
            final ModuleParam properties) {
        super(manager, info, properties);

        String wikiPath = properties.getString(WIKI_DIR, null);
        if(wikiPath == null) {
            throw new IllegalArgumentException(WIKI_DIR + " property not found!");
        }

        wikiDir = new File(wikiPath);
        if(!wikiDir.exists() && !wikiDir.mkdirs()) {
            throw new IllegalArgumentException("Can not create wiki dir: " + wikiDir);
        }
        if(!wikiDir.canRead()) {
            throw new IllegalArgumentException("Can not read wiki dir: " + wikiDir);
        }

        cacheDir = properties.getFile(WIKI_CACHE_DIR, (File)null);
        if(cacheDir != null && !cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IllegalArgumentException("Can not create cache dir: " + cacheDir);
        }
    }

    @Override
    public void process(
            final ServletContext context,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final ModuleParam params,
            final HashMap<String, FileItem> files) throws IOException {

        final ResourceModule resourceModule = getManager().getModuleOrThrow(ResourceModule.class);

        if(cacheDir == null) {
            cacheDir = resourceModule.initTempDir();
        }

        if(params.containsKey("get") || params.containsKey("html") /* deprecated */) {
            final String path = params.getString("get", params.getString("html", null)).replaceAll("[\\s]", "_");

            if(path.matches(IMAGE_REGEX)) { // Is image?
                resourceModule.writeFile(response, new File(wikiDir, path));
            } else {
                final File cacheFile = new File(cacheDir, path + ".html");
                final File markdown = new File(wikiDir, path + ".md");
                final File mediawiki = new File(wikiDir, path + ".mediawiki");

                if(markdown.exists()) {
                    if(!cacheFile.exists() || markdown.lastModified() > cacheFile.lastModified()) {
                        markdownConvert(context, markdown, cacheFile);
                        Tools.zipFile(cacheFile, new File(cacheFile.getPath() + ".gz"));
                    }
                } else if(mediawiki.exists()) {
                    if(!cacheFile.exists() || mediawiki.lastModified() > cacheFile.lastModified()) {
                        mediawikiConvert(context, mediawiki, cacheFile);
                        Tools.zipFile(cacheFile, new File(cacheFile.getPath() + ".gz"));
                    }
                }

                response.addHeader("Content-type", "text/html");
                resourceModule.writeFile(request, response, cacheFile);
            }
        } else if(params.containsKey("put") || files != null && files.size() > 0) {
            // TODO: Edit mode does not support yet
            /*if(files != null && files.size() > 0) {
                for (Map.Entry<String, FileItem> f:files.entrySet()) {

                }
            }

            final String put = params.getString("put", null);
            if(put != null) {
                String title = params.getString("title", null);
                String format = params.getString("format", "mediawiki");

                if(title == null || !Tools.isValidFilename(title)) {
                    throw new ModuleException("title is not valid: " + title);
                }

                if("mediawiki".equals(format)) {

                } else {
                    throw new ModuleException("Illegal format");
                }
            }*/
        } else if(params.containsKey("clear_cache")) {
            File[] _files = cacheDir.listFiles();
            if(_files != null) {
                for(File f:_files) {
                    Files.deleteIfExists(f.toPath());
                }
            }
        } else {
            throw new IllegalArgumentException("Illegal request!");
        }

    }

    private void markdownConvert(final ServletContext context, final File src, final File dst) throws IOException {
        final String wiki = Tools.readTextFile(src);
        final String html = new Markdown4jProcessor().process(wiki);
        Tools.writeTextFile(html, dst);
    }

    private void mediawikiConvert(final ServletContext context, final File src, final File dst) throws IOException {
        final String wiki = Tools.readTextFile(src);

        // generate image address
        final String apiPath = (context.getContextPath() != null ? context.getContextPath() : "") +
                Constants.MODULE_URI_PERFIX + "?" +
                Constants.MODULE_NAME_PARAMETER + "=" + getInfo().getName() + "&get=";

        final WikiModel model = new WikiModel(apiPath + "${image}", "${title}");

        String html = model.render(wiki, false);

        // there's some problem with WikiModel, if you just put [[File:example.jpg|caption]]
        // you will get
        // <a ... href="File:example.jpg" ...>
        // that is actually no where! so we fix it by this regex

        html = html.replaceAll("(?i)href=\"file:", "href=\"" + apiPath);

        final String attributesToRemove = "width";

        html = html.replaceAll("\\s+(?:" + attributesToRemove + ")\\s*=\\s*\"[^\"]*\"","");

        // another solution remove attr,
        // htmlFragment.replaceAll("\\s+(?:" + attributesToRemove + ")\\s*=\\s*\"[^\"]*\"","");
        // code:
        // String attributesToRemove = "id\\=\\\"3nav";
        // html = html.replaceAll("\\s+(?:" + attributesToRemove + ")[^\"]*\"","");

        // Fix toc
        // We don't need TITLE, because it's different in different language. We add title by CSS
        final int startTocIndex = html.indexOf("<div id=\"toctitle\">");
        if(startTocIndex > 0) {
            final int endTocIndex = html.indexOf("</div>") + "</div>".length();
            html = html.substring(0, startTocIndex) + html.substring(endTocIndex);
        }

        Tools.writeTextFile(html, dst);
    }

}
