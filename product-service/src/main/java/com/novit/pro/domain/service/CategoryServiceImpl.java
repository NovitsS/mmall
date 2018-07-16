package com.novit.pro.domain.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.novit.pro.common.ServerResponse;
import com.novit.pro.domain.model.Category;
import com.novit.pro.domain.repository.CategoryMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId){
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){//空判断，若categoryList为空
            logger.info("未找到当前分类的子分类");//写进日志中
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点的id及孩子节点的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();//Sets是guava提供的对set的处理方法
        findChildCategory(categorySet,categoryId);//调用递归算法

        List<Integer> categoryIdList = Lists.newArrayList();//Lists也是guava提供的方法
        if(categoryId != null){
            for(Category categoryItem : categorySet){//遍历set
                categoryIdList.add(categoryItem.getId());//向list中添加id
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    //递归算法,，自己调用自己，算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet , Integer categoryId){//把这个参数当作返回值返回给这个方法本身，然后拿这个方法本身的返回值再调用这个方法，当成方法的参数
        Category category = categoryMapper.selectByPrimaryKey(categoryId);//用categoryId查询
        if(category != null){
            categorySet.add(category);//向categorySet里面添加category
        }
        //查找子节点,递归算法一定要有一个退出的条件，退出条件就是子节点是否为空
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);//用集合接受
        for(Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;//如果categoryList为空的话就进不来for循环，直接return
    }
}
