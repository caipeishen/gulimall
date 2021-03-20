package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Author: Cai Peishen
 * @Date: 2021/3/19 21:48
 * @Description:
 **/
@Slf4j
@Controller
public class CartController {

    private final String PATH = "redirect:http://cart.gulimall.com/cart.html";

    @Autowired
    private CartService cartService;

    /**
     * 购物车界面
     * 浏览器有一个cookie：user-key 标识用户身份 一个月后过期
     * 每次访问都会带上这个 user-key
     * 如果没有临时用户 还要帮忙创建一个
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping({"/","/cart.html"})
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = this.cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * 	RedirectAttributes: 会自动将数据添加到url后面
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        this.cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        // 重定向到成功页面
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 添加购物车成功界面
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam(value = "skuId",required = false) Object skuId, Model model){
        CartItem cartItem = null;
        // 然后在查一遍 购物车
        if(skuId == null){
            model.addAttribute("item", null);
        }else{
            try {
                cartItem = this.cartService.getCartItem(Long.parseLong((String)skuId));
            } catch (NumberFormatException e) {
                log.warn("恶意操作! 页面传来非法字符.");
            }
            model.addAttribute("item", cartItem);
        }
        return "success";
    }

    /**
     * 选中购物车项
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("checkItem.html")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check){
        this.cartService.checkItem(skuId, check);
        return PATH;
    }

    /**
     * 改变购物项的个数
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num){
        this.cartService.changeItemCount(skuId, num);
        return PATH;
    }

    /**
     * 删除购物项
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        this.cartService.deleteItem(skuId);
        return PATH;
    }

    /**
     * 获取当前用户的所有购物项
     * @return
     */
    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){
        return this.cartService.getUserCartItems();
    }

    /**
     * 去结算
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ResponseBody
    @GetMapping("toTrade")
    public String toTrade() throws ExecutionException, InterruptedException {
        BigDecimal price = this.cartService.toTrade();
        return "结算成功,共：￥" + price.toString();
    }

}
