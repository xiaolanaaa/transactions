package com.huohu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huohu.pojo.JsonRes;
import com.huohu.pojo.Position;
import com.huohu.pojo.Tran;
import com.huohu.service.PositionService;
import com.huohu.service.TranService;
import com.huohu.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("tran")
public class TranController {
    @Autowired
    TranService tranService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PositionService positionService;
    @Autowired
    UserService userService;

    //委托挂单接口

    @RequestMapping("add")
    public JsonRes add(@RequestBody Tran tran) {
        //买入数量
        Integer buynum = 0;
        //卖出数量
        Integer sell = 0;
        //买入总价
        Integer buySalary = 0;
        //卖出总价
        Integer sellSalary = 0;
        //判断当前是买入还是卖出
        if (tran.getPut().equals("buy")) {
            //记录当前买入手数
            buynum = tran.getNum();
            //买入所需总金额
            buySalary = buynum * tran.getSalary() + tran.getNum() * 1;
            //现有资金减去买入总价

            //判断当前用户是否买入过当前股票

            //输出一下买入还是卖出

            //给当前订单状态赋值
            tran.setTime(new Date());
            tran.setStatus("待处理");
            tran.setDealnum(0);
            tran.setUncompleted(tran.getNum());
            //存入数据库
            tranService.save(tran);

            //存入redis中
            List<Tran> buyList = (List<Tran>) redisTemplate.boundValueOps("buyList").get();

            if (buyList != null) {
                buyList.add(tran);
                redisTemplate.boundValueOps("buylist").set(buyList);
                rabbitTemplate.convertAndSend("tranlist", buyList);
            } else {
                List<Tran> trans = new ArrayList<>();
                trans.add(tran);
                redisTemplate.boundValueOps("buyList").set(trans);
                rabbitTemplate.convertAndSend("tranlist", trans);
            }
            return JsonRes.success("委托买入成功");


        } else {
            //记录当前卖出手数
            sell = tran.getNum();
            //计算卖出总价
            sellSalary = sell * tran.getSalary() - tran.getNum() * 1;
            //现有资金+卖出总价

            //输出一下买入还是卖出

            //判断是否有当前股票持仓 没有则无法卖出
            LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
            positionLambdaQueryWrapper.eq(Position::getTranname, tran.getName());
            positionLambdaQueryWrapper.eq(Position::getUserid, tran.getUserid());
            Position one = positionService.getOne(positionLambdaQueryWrapper);
            if (one != null) {




                if (one.getNumber() >= tran.getNum()) {
                    tran.setStatus("待处理");
                    tran.setTime(new Date());
                    tran.setDealnum(0);
                    tran.setUncompleted(tran.getNum());

                    tranService.save(tran);
                    //存入redis中
                    List<Tran> sellList = (List<Tran>) redisTemplate.boundValueOps("sellList").get();
                    if (sellList != null) {
                        for (Tran tran1 : sellList) {
                            if (tran.getName().equals(tran1.getName())) {
                                tran1.setNum(tran1.getNum() + tran.getNum());
                            } else {
                                sellList.add(tran);

                            }
                        }
                        redisTemplate.boundValueOps("sellList").set(sellList);

                    } else {
                        List<Tran> trans=new ArrayList<>();
                        trans.add(tran);
                        redisTemplate.boundValueOps("sellList").set(trans);

                    }
                    //更新持仓数量
                    one.setNumber(one.getNumber()-tran.getNum());
                    one.setSalary((one.getSalary()+ tran.getSalary())/2);
                    //将更新过的持仓数量修改至数据库
                    positionService.updateById(one);
                } else {
                    return JsonRes.error("持仓数量不够无法卖出");
                }
            } else {
                return JsonRes.error("没有当前股票持仓无法卖出");
            }
            //存入数据库



            return JsonRes.success("挂单成功");


        }

    }

    //撤单接口
    @RequestMapping("revoke")
    public void revoke() {
        //获取到订单队列

        //撤单查询当前订单是否完成如果完成则无法撤单

        //，部分完成则将未完成的部分从订单队列中删除掉

        //redis中删除之后再从数据库中删除订单信息

        //返回成功信息
    }

    //撮合交易
    @RabbitListener(queues = "tranlist")
    public void matchmaking() {
        //设置变量标识当前缓存队列是否发生改变
        boolean good = false;
        //首先获取订单队列
        List<Tran> trans = tranService.findbuyList();
        List<Tran> selllist = tranService.findsellList();
        for (Tran tran : trans) {
            for (Tran tran1 :selllist ) {

                    if (tran.getName() .equals(tran1.getName()) ) {
                        //大于则进行撮合交易
                        if (tran.getSalary() <= tran1.getSalary()) {
                            if (tran.getNum()<=tran1.getNum()){
                                //买入数量小于等于卖出数量
                                //成交手数
                                tran.setDealnum(tran.getNum());
                                //未成交手数
                                tran.setUncompleted(tran.getNum()-tran.getDealnum());

                                tran1.setDealnum(tran.getDealnum());
                                tran1.setUncompleted(tran1.getNum()-tran1.getDealnum());

                                tranService.updateById(tran);

                                if (tran.getUncompleted()==0){
                                    //删除数据库中的挂单信息
                                    tranService.removeById(tran.getId());
                                    //从队列中删除完成的挂单
                                    selllist.remove(tran);

                                }

                                if (tran1.getUncompleted()==0){
                                    //删除数据库中的挂单信息
                                    tranService.removeById(tran1.getId());
                                    //从队列中删除完成的挂单
                                    trans.remove(tran1);
                                }

                            }else {
                                //卖出数量大于买入数量

                                //成交手数
                                tran1.setDealnum(tran1.getNum());
                                //未成交手数
                                tran1.setUncompleted(tran1.getNum()-tran.getDealnum());
                                tranService.updateById(tran1);
                                if (tran.getUncompleted()==0){
                                    //删除数据库中的挂单信息
                                    tranService.removeById(tran.getId());
                                    //从队列中删除完成的挂单
                                    selllist.remove(tran);

                                }
                                if (tran1.getUncompleted()==0){
                                    //删除数据库中的挂单信息
                                    tranService.removeById(tran1.getId());
                                    //从队列中删除完成的挂单
                                    trans.remove(tran1);
                                }

                            }
                            //修改持仓数量
                            LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                            positionLambdaQueryWrapper.eq(Position::getUserid,2);
                            positionLambdaQueryWrapper.eq(Position::getTranname,tran.getName());
                            Position one = positionService.getOne(positionLambdaQueryWrapper);
                            if (one!=null){
                                one.setNumber(one.getNumber()+tran.getDealnum());
                                one.setSalary(one.getSalary()+tran.getSalary()/2);
                                positionService.updateById(one);
                            }else {
                                Position position = new Position();
                                position.setTime(new Date());
                                position.setTranname(tran.getName());
                                position.setNumber(tran.getDealnum());
                                position.setSalary(tran.getSalary());
                                positionService.save(position);
                            }


                        }
                    }


            }
        }
        redisTemplate.boundValueOps("sellList").set(selllist);


        //返回部分完成的数据

        //撮合完成返回信息


    }
}