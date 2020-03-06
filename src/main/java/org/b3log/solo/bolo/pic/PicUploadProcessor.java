package org.b3log.solo.bolo.pic;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.solo.bolo.pic.util.UploadUtil;
import org.b3log.solo.model.Option;
import org.b3log.solo.repository.OptionRepository;
import org.b3log.solo.util.Solos;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>bolo-solo</h3>
 * <p>图床上传</p>
 *
 * @author : https://github.com/AdlerED
 * @date : 2020-03-04 20:50
 **/
@RequestProcessor
public class PicUploadProcessor {

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    @RequestProcessing(value = "/pic/upload", method = {HttpMethod.POST})
    public void uploadPicture(final RequestContext context) {
        if (!Solos.isAdminLoggedIn(context)) {
            context.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File("temp/"));
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        Map okPic = new HashMap();
        List<String> errFiles = new ArrayList<>();
        try {
            List<FileItem> itemList = upload.parseRequest(context.getRequest());
            for (FileItem item : itemList) {
                String name = item.getName();
                String config = "hacpai";
                try {
                    config = optionRepository.get(Option.ID_C_TUCHUANG_CONFIG).optString(Option.OPTION_VALUE);
                } catch (Exception e) {
                }
                File file = new File("temp/" + name);
                item.write(file);
                try {
                    String url = UploadUtil.upload(config, file);
                    okPic.put(name, url);
                } catch (Exception e) {
                    errFiles.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map map = new HashMap();
        map.put("succMap", okPic);
        map.put("errFiles", errFiles);
        context.renderJSON().renderData(map);
        context.renderCode(0);
        context.renderMsg("");
    }
}
