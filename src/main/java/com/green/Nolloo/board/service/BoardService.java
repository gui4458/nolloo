package com.green.Nolloo.board.service;

import com.green.Nolloo.board.vo.BoardVO;

import java.util.List;

public interface BoardService {
    // board 등록하는 메소드
    void insertBoard(BoardVO boardVO);
    // board 목록 조회 메소드
    List<BoardVO> selectBoardList();
    // board 디테일
    BoardVO selectBoardDetail(BoardVO boardVO);
    //board 삭제하는 메소드
    void deleteBoard(BoardVO boardVO);

    void updateBoard(BoardVO boardVO);

}
