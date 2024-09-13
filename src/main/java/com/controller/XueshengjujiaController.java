
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 居家
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/xueshengjujia")
public class XueshengjujiaController {
    private static final Logger logger = LoggerFactory.getLogger(XueshengjujiaController.class);

    @Autowired
    private XueshengjujiaService xueshengjujiaService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private XueshengService xueshengService;

    @Autowired
    private LaoshiService laoshiService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("学生".equals(role))
            params.put("xueshengId",request.getSession().getAttribute("userId"));
        else if("老师".equals(role))
            params.put("laoshiId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = xueshengjujiaService.queryPage(params);

        //字典表数据转换
        List<XueshengjujiaView> list =(List<XueshengjujiaView>)page.getList();
        for(XueshengjujiaView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        XueshengjujiaEntity xueshengjujia = xueshengjujiaService.selectById(id);
        if(xueshengjujia !=null){
            //entity转view
            XueshengjujiaView view = new XueshengjujiaView();
            BeanUtils.copyProperties( xueshengjujia , view );//把实体数据重构到view中

                //级联表
                XueshengEntity xuesheng = xueshengService.selectById(xueshengjujia.getXueshengId());
                if(xuesheng != null){
                    BeanUtils.copyProperties( xuesheng , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setXueshengId(xuesheng.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody XueshengjujiaEntity xueshengjujia, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,xueshengjujia:{}",this.getClass().getName(),xueshengjujia.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("学生".equals(role))
            xueshengjujia.setXueshengId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<XueshengjujiaEntity> queryWrapper = new EntityWrapper<XueshengjujiaEntity>()
            .eq("xuesheng_id", xueshengjujia.getXueshengId())
            .eq("xueshengjujia_zhuangtai_types", xueshengjujia.getXueshengjujiaZhuangtaiTypes())
            .eq("jujia_time", new SimpleDateFormat("yyyy-MM-dd").format(xueshengjujia.getJujiaTime()))
            .eq("xueshengjujia_address", xueshengjujia.getXueshengjujiaAddress())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        XueshengjujiaEntity xueshengjujiaEntity = xueshengjujiaService.selectOne(queryWrapper);
        if(xueshengjujiaEntity==null){
            xueshengjujia.setCreateTime(new Date());
            xueshengjujiaService.insert(xueshengjujia);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody XueshengjujiaEntity xueshengjujia, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,xueshengjujia:{}",this.getClass().getName(),xueshengjujia.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("学生".equals(role))
//            xueshengjujia.setXueshengId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<XueshengjujiaEntity> queryWrapper = new EntityWrapper<XueshengjujiaEntity>()
            .notIn("id",xueshengjujia.getId())
            .andNew()
            .eq("xuesheng_id", xueshengjujia.getXueshengId())
            .eq("xueshengjujia_zhuangtai_types", xueshengjujia.getXueshengjujiaZhuangtaiTypes())
            .eq("jujia_time", new SimpleDateFormat("yyyy-MM-dd").format(xueshengjujia.getJujiaTime()))
            .eq("xueshengjujia_address", xueshengjujia.getXueshengjujiaAddress())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        XueshengjujiaEntity xueshengjujiaEntity = xueshengjujiaService.selectOne(queryWrapper);
        if(xueshengjujiaEntity==null){
            xueshengjujiaService.updateById(xueshengjujia);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        xueshengjujiaService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<XueshengjujiaEntity> xueshengjujiaList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("../../upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            XueshengjujiaEntity xueshengjujiaEntity = new XueshengjujiaEntity();
//                            xueshengjujiaEntity.setXueshengId(Integer.valueOf(data.get(0)));   //学生 要改的
//                            xueshengjujiaEntity.setXueshengjujiaZhuangtaiTypes(Integer.valueOf(data.get(0)));   //居家状态 要改的
//                            xueshengjujiaEntity.setJujiaTime(sdf.parse(data.get(0)));          //居家日期 要改的
//                            xueshengjujiaEntity.setXueshengjujiaAddress(data.get(0));                    //居家位置 要改的
//                            xueshengjujiaEntity.setXueshengjujiaContent("");//详情和图片
//                            xueshengjujiaEntity.setCreateTime(date);//时间
                            xueshengjujiaList.add(xueshengjujiaEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        xueshengjujiaService.insertBatch(xueshengjujiaList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
