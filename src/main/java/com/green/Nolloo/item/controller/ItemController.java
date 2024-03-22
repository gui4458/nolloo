package com.green.Nolloo.item.controller;

import com.green.Nolloo.chat.service.ChatService;
import com.green.Nolloo.chat.vo.ChatVO;
import com.green.Nolloo.item.service.ItemService;
import com.green.Nolloo.item.vo.ImgVO;
import com.green.Nolloo.item.vo.ItemVO;
import com.green.Nolloo.member.service.MemberService;
import com.green.Nolloo.member.vo.MemberImageVO;
import com.green.Nolloo.member.vo.MemberVO;

import com.green.Nolloo.reserve.service.ReserveService;
import com.green.Nolloo.reserve.vo.ReserveVO;

import com.green.Nolloo.restAPI.service.KakaoApiService;
import com.green.Nolloo.restAPI.vo.AddressVO;
import com.green.Nolloo.restAPI.vo.MapVO;

import com.green.Nolloo.search.vo.SearchVO;
import com.green.Nolloo.util.PathVariable;
import com.green.Nolloo.util.UploadUtil;
import com.green.Nolloo.wish.service.WishService;
import com.green.Nolloo.wish.vo.WishViewVO;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.standard.processor.AbstractStandardDoubleAttributeModifierTagProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/item")
public class ItemController {

    @Resource(name = "itemService")
    private ItemService itemService;
    @Resource(name = "wishService")
    private WishService wishService;


    @Resource(name="reserveService")
    private ReserveService reserveService;


    @Resource(name = "memberService")
    private MemberService memberService;
    @Resource(name = "chatService")
    private ChatService chatService;

    @Autowired
    private KakaoApiService kakaoApiService;

    //파티게시글 목록조회
    @RequestMapping("/list")
    public String list(Model model, Authentication authentication, ItemVO itemVO
                        , @RequestParam(name="chkCode",required = false,defaultValue = "2")int chkCode
                        ,SearchVO searchVO,HttpSession session){
        searchVO.setCateCode(chkCode);
        model.addAttribute("itemList",itemService.selectPartyList(searchVO));
        //        전체 데이터 수
        searchVO.setCateCode(chkCode);
        int totalDataCnt = itemService.itemAllCnt(searchVO.getCateCode());
        searchVO.setTotalDataCnt(totalDataCnt);
//        페이지 정보 세팅
        searchVO.setPageInfo();
        List<Integer> wishCodeList = new ArrayList<>();
        model.addAttribute("chkCode",chkCode);



        if (authentication != null){
            User user = (User)authentication.getPrincipal();

            List<WishViewVO> wishList = wishService.selectWish(user.getUsername());
            for (WishViewVO e : wishList){
                wishCodeList.add(e.getItemCode());
            }
            model.addAttribute("wishCodeList",wishCodeList);
        session.setAttribute("memberImage",memberService.selectProfile(user.getUsername()));
        session.setAttribute("memberId",user.getUsername());
        }

        return "content/main";
    }
    //게시글 등록
    @GetMapping("/itemAddForm")
    public String boardAddForm(){


        return"content/item/item_add_form";
    }
    @PostMapping("/itemAdd")
    public String boardAdd(ItemVO itemVO
                            , @RequestParam(name = "img") MultipartFile img
                            , @RequestParam(name = "imgs") MultipartFile[] imgs
                            , @RequestParam(name="itemPlace") String addr
                            , Authentication authentication, ChatVO chatVO){

        User user = (User)authentication.getPrincipal();
        itemVO.setMemberId(user.getUsername());
        //메인이미지 업로드
        ImgVO mainImg = UploadUtil.uploadFile(img);
        //상세이미지 업로드
        List<ImgVO> imgList =UploadUtil.multiUploadFile(imgs);

        int itemCode = itemService.selectNextItemCode();
        chatVO.setItemCode(itemCode);
        chatVO.setRoomName(itemVO.getItemTitle());
        chatVO.setFounder(user.getUsername());
        itemVO.setChatVO(chatVO);
        itemVO.setItemCode(itemCode);

        mainImg.setItemCode(itemCode);

        for (ImgVO subImg : imgList){
            subImg.setItemCode(itemCode);
        }
        imgList.add(mainImg);
        itemVO.setImgList(imgList);
        System.out.println(itemVO);
        MapVO mapVO = kakaoApiService.getGeoFromAddress(addr);

        System.out.println(mapVO);

        double lat = mapVO.getItemX();
        double lng = mapVO.getItemY();

        System.out.println("Lat:" + lat +" Lng:"+ lng);

        itemVO.setItemX(lat);
        itemVO.setItemY(lng);

        itemService.insertParty(itemVO);
        return "redirect:/item/list";
    }

    //itemDetail 조회
    @GetMapping("/itemDetailForm")
    public String boardDetailForm(ItemVO itemVO, ReserveVO reserveVO, Model model, Authentication authentication
            , @RequestParam(name="chkCode",required = false,defaultValue = "1")int chkCode){

        itemService.itemListUpdateCnt(itemVO);
        model.addAttribute("item",itemService.selectPartyDetail(itemVO));

        model.addAttribute("chkCode",chkCode);

        if (authentication != null){
            User user = (User)authentication.getPrincipal();
            System.out.println(user.getUsername());
            reserveVO.setMemberId(user.getUsername());
            model.addAttribute("reserveCnt",reserveService.reserveDone(reserveVO));
        }

        return "content/item/item_detail";
    }
    //게시글 삭제
    @GetMapping("/deleteItem")
    public String deleteParty(ItemVO itemVO){
        itemService.deleteParty(itemVO);
        return "redirect:/item/list";
    }

    @GetMapping("/updateForm")
    public String updateForm(Model model, ItemVO itemVO){
        model.addAttribute("item",itemVO);
        return "content/item/item_update_form";
    }

    @PostMapping("/updateParty")
    public String updateParty(ItemVO itemVO){
        itemService.updateParty(itemVO);
        return "redirect:/item/itemDetailForm?itemCode="+itemVO.getItemCode();
    }





    @GetMapping("/myParty")
    public String myParty(ItemVO itemVO, Model model,HttpSession session){

        String memberId = (String)session.getAttribute("memberId");
        itemVO.setMemberId(memberId);
        List<ItemVO> myPartyList = itemService.selectMyParty(itemVO);
        model.addAttribute("myPartyList",myPartyList);
        return "content/member/my_party";
    }

    @ResponseBody
    @PostMapping("/selectItemDetail")
    public ItemVO selectItemDetail(@RequestParam(name="itemCode") int itemCode,ItemVO itemVO){
        ItemVO detail=itemService.selectItemDetail(itemCode);
        System.out.println(itemCode);
        System.out.println(detail);


        return detail;
    }

    @PostMapping("/updateItem")
    public String updateItem(ItemVO itemVO){
        System.out.println(itemVO);
        itemService.updateItemDetail(itemVO);


        return "redirect:/item/myParty";
    }

    //상품 상세보기 페이지에서 이미지 삭제버튼 클릭 시 실행
    @ResponseBody
    @PostMapping("/deleteImg")
    public void deleteImg(ImgVO imgVO){
        System.out.println(imgVO);
        //첨부파일명 조회
        String attachedFileName = itemService.findAttachedFileNameByImgCode(imgVO);

        //선택한 이미지 디비에서 삭제
        //itemService.deleteItemImg(imgVO);

        //첨부파일 삭제
        //UploadUtil.deleteUploadFile(PathVariable.ITEM_UPLOAD_PATH + attachedFileName);



    }



}
