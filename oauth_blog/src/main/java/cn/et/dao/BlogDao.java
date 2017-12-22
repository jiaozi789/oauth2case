package cn.et.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.et.entity.Blog;

public interface BlogDao extends CrudRepository<Blog, Integer> {

	@Query(value="from Blog where title like %:name% or content like %:name%",nativeQuery=false)
	public List<Blog> queryByContent(@Param("name") String name);
}
