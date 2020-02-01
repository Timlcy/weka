package weka.initData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName FileData
 * @Description
 * @Author 林春永
 * @Date 2020/2/1
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/fileData")
@Api(value = "文件接口Controller")
public class FileData extends GeneralData {
    @ApiOperation(value = "文件导入")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required =
                    true, paramType = "query", dataType = "MultipartFile"
            )
    })
    @PostMapping("fileImport")
    public boolean fileImport(@RequestParam("file") MultipartFile file
    ) {
        if (!file.isEmpty()) {
            try {
                InputStream is = file.getInputStream();
                Instances read = ConverterUtils.DataSource.read(is);
                read.setClassIndex(read.numAttributes() - 1);
                setInstances(read);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        } else {
            return false;
        }
    }

}
