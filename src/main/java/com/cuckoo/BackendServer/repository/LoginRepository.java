package com.cuckoo.BackendServer.repository;

import java.util.List;
import com.cuckoo.BackendServer.exceptions.UnknownUserException;
import com.cuckoo.BackendServer.exceptions.UserAlreadyExistsException;
import com.cuckoo.BackendServer.exceptions.UsernameEmptyException;
import com.cuckoo.BackendServer.exceptions.WrongPasswordException;
import com.cuckoo.BackendServer.exceptions.DatabaseException;
import com.cuckoo.BackendServer.exceptions.InvalidArgumentsException;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.models.usertype.UserTypeMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;


@Repository
public class LoginRepository{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder encoder;



    /*returns true if the user exists or false otherwise*/
    private boolean doesUserExist(String username){
        try{
            this.getUserByUsername(username);
        } catch(UnknownUserException e){
            return false;
        }
        return true;
    }
    
    public void createUserInDatabase(UserType user) {
        String sql1 = "INSERT INTO cuckoo.users(email,first_name,last_name,pass) VALUES (?,?,?,?);";

        if(this.doesUserExist(user.getUsername())){
            throw new UserAlreadyExistsException(user.getUsername());
        }
        try{
            this.jdbcTemplate.update(sql1,user.getUsername(),user.getFirstName(),user.getLastName(),user.getPassword());
        } catch (DataAccessException e){
            throw new DatabaseException("Unable insert user on createUserInDatabase");
        }
        
    }

    public UserType getUserByUsername(String username){
        String sql = "SELECT email, pass, first_name, last_name FROM cuckoo.users WHERE email = ?";
        List<UserType> res = null;
        res = this.jdbcTemplate.query(sql, new Object[] {username}, new UserTypeMapper());
        if(res.isEmpty()){
            throw new UnknownUserException(username);
        }
        return res.get(0);
    }

    public void removeUserInDatabase(UserType user) {
        String sql = "DELETE FROM cuckoo.users WHERE email = ?";
        UserType auxUser = this.getUserByUsername(user.getUsername());
        
        if(!this.encoder.matches(user.getPassword(), auxUser.getPassword())){
            throw new WrongPasswordException(user.getUsername());
        }
        try{
            this.jdbcTemplate.update(sql,user.getUsername());
        } catch (DataAccessException e){
            throw new DatabaseException("Unable to delete user at removeUserInDatabase");
        }
    }

    public UserType getUserInfo(String username){
        if(username == null)
          throw new UsernameEmptyException("username can not be null for getUserInfo");
        String sql = "SELECT email, first_name, last_name FROM cuckoo.users WHERE email = ?";
        List<UserType> res = null;
        res = this.jdbcTemplate.query(sql, new Object[] {username}, new UserTypeMapper());
        if(res.isEmpty()){
            throw new UnknownUserException(username);
        }
        return res.get(0);
    }
    
}