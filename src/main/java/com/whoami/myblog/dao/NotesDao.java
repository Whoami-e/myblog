package com.whoami.myblog.dao;

import com.whoami.myblog.entity.Notes;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotesDao {
    int deleteByPrimaryKey(String id);

    int insert(Notes record);

    int insertSelective(Notes record);

    Notes selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Notes record);

    int updateByPrimaryKey(Notes record);

    List<Notes> listNotes();
}