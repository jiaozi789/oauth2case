package cn.et.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.et.entity.Client;

public interface ClientDao extends CrudRepository<Client, Integer> {
	@Query("from Client where clientId=:clientId and clientSecret=:clientSecret")
	public List<Client> queryClientByClientId(@Param("clientId")String clientId,@Param("clientSecret")String clientSecret);
	@Query("from Client where clientId=:clientId and clientSecret=:clientSecret and userId=:userId")
	public List<Client> queryClient(@Param("clientId")String clientId,@Param("clientSecret")String clientSecret,@Param("userId")String userId);

}
