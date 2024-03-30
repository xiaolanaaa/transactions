package com.huohu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huohu.pojo.*;
import com.huohu.service.PositionService;
import com.huohu.service.ReportService;
import com.huohu.service.TranService;
import com.huohu.service.UserService;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;
import org.slf4j.Logger;

@RestController
@RequestMapping("tran")
public class TranController {
    //委托挂单
    @Autowired
    TranService tranService;
    //redis缓存
    @Autowired
    RedisTemplate redisTemplate;
    //rabbitMQ缓存
    @Autowired
    RabbitTemplate rabbitTemplate;
    //持仓
    @Autowired
    PositionService positionService;
    //用户接口
    @Autowired
    UserService userService;
    //成交订单
    @Autowired
    ReportService reportService;
    private static final Logger logger = LoggerFactory.getLogger(TranController.class);

    //委托挂单
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
            tran.setStatus("委托中");
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
            //获取当前用户

            //现有资金+卖出总价

            //输出一下买入还是卖出

            //判断是否有当前股票持仓 没有则无法卖出
            LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
            positionLambdaQueryWrapper.eq(Position::getTranname, tran.getName());
            positionLambdaQueryWrapper.eq(Position::getUserid, tran.getUserid());
            Position one = positionService.getOne(positionLambdaQueryWrapper);
            if (one != null) {
                if (one.getNumber() >= tran.getNum()) {
                    tran.setStatus("委托中");
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
                        List<Tran> trans = new ArrayList<>();
                        trans.add(tran);
                        redisTemplate.boundValueOps("sellList").set(trans);
                    }
                    //更新持仓数量
                    one.setNumber(one.getNumber() - tran.getNum());
                    one.setSalary((one.getSalary() + tran.getSalary()) / 2);
                    //将更新过的持仓数量修改至数据库
                    rabbitTemplate.convertAndSend("tranlist", tran);
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
    @RequestMapping("revoke")
    public JsonRes revoke(@RequestBody Tran tran) {
        // 验证输入是否为空
        if (tran == null || tran.getId() == null) {
            return JsonRes.error("参数错误");
        }

        // 使用Optional简化空检查
        Optional<Tran> byIdOptional = Optional.ofNullable(tranService.getById(tran.getId()));

        if (byIdOptional.isPresent()) {
            Tran byId = byIdOptional.get();

            // 检查订单是否已完成
            if (byId.getUncompleted()==0) {
                return JsonRes.error("当前订单已完成撮合，无法撤回");
            }

            // 计算退款金额（示例，需根据实际业务逻辑调整）
            int refund = calculateRefund(byId);

            // 更新持仓数量（如果适用）
            updatePosition(byId);

            // 删除委托挂单
            tranService.removeById(tran.getId());

            // 添加事务控制以保证数据一致性（示例假设有一个@Transactional注解的环境）
            // ...

            logger.info("撤单成功, 退款金额: {}", refund);
            return JsonRes.success("撤单成功");
        } else {
            return JsonRes.error("订单不存在");
        }
    }

    private int calculateRefund(Tran tran) {
        // 请根据实际业务逻辑调整以下计算
        return tran.getUncompleted() * tran.getSalary() + tran.getUncompleted();
    }

    private void updatePosition(Tran tran) {
        // 使用Lambda表达式简化查询
        LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        positionLambdaQueryWrapper.eq(Position::getTranname, tran.getName());
        positionLambdaQueryWrapper.eq(Position::getUserid, tran.getUserid());

        Optional<Position> oneOptional = Optional.ofNullable(positionService.getOne(positionLambdaQueryWrapper));

        if (oneOptional.isPresent()) {
            Position one = oneOptional.get();
            one.setNumber(one.getNumber() + tran.getUncompleted());
            positionService.updateById(one);
            logger.info("持仓数量更新成功, 新数量: {}", one.getNumber());
        }
    }
    //撤单接口
//    @RequestMapping("revoke")
//    public JsonRes revoke(@RequestBody Tran tran) {
//        //定义退款金额变量
//        Integer refund = 0;
//        //获取到订单队列
//        if (tran != null) {
//            Tran byId = tranService.getById(tran.getId());
//            //撤单查询当前订单是否完成如果完成则无法撤单
//            if (byId != null) {
//                //部分完成则将未完成的部分从订单中删除掉
//
//                //需要退还的保证金以及手续费
//                refund = (byId.getUncompleted() * byId.getSalary()) + byId.getUncompleted();
//                //当前用户资金+退还的保证金以及手续费
//                if (byId.getPut().equals("sell")) {
//                    //更新持仓数量
//                    LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    positionLambdaQueryWrapper.eq(Position::getTranname, byId.getName());
//                    positionLambdaQueryWrapper.eq(Position::getUserid, byId.getUserid());
//                    Position one = positionService.getOne(positionLambdaQueryWrapper);
//                    if (one != null) {
//                        one.setNumber(one.getNumber() + byId.getUncompleted());
//                        positionService.updateById(one);
//                    }
//                }
//                //删除数据库中的委托挂单
//                tranService.removeById(tran.getId());
//            } else {
//                return JsonRes.error("当前订单已完成撮合，无法撤回");
//            }
//        } else {
//            return JsonRes.error("参数错误");
//        }
//        //返回成功信息
//        return JsonRes.success("撤单成功");
//    }

    //撮合交易
    @RabbitListener(queues = "tranlist")
    public void matchmaking() {
        //设置变量
        boolean good = false;
        //首先获取订单队列
        //买入订单
        List<Tran> trans = tranService.findbuyList();
        //卖出订单
        List<Tran> selllist = tranService.findsellList();
        for (Tran tran : trans) {
            for (Tran tran1 : selllist) {
                //买入价格必须大于卖出价
               if (tran.getSalary()>=tran1.getSalary()){
                   //股票产品是否一样
                   if (tran.getName().equals(tran1.getName())) {
                       //大于则进行撮合交易
                       if (tran.getUncompleted() >= tran1.getUncompleted()) {
                           //买入数量大于等于卖出数量
                           //买入成交手数
                           tran.setDealnum(tran1.getUncompleted());
                           //买入未成交手数
                           tran.setUncompleted(tran.getUncompleted() - tran.getDealnum());
                           //卖出成交数量
                           tran1.setDealnum(tran.getDealnum());
                           //卖出未成交手数
                           tran1.setUncompleted(tran1.getUncompleted() - tran.getDealnum());
                           tranService.updateById(tran);
                           tranService.updateById(tran1);
                           if (tran.getUncompleted() == 0) {
                               //删除数据库中的挂单信息
                               tranService.removeById(tran.getId());
                               //从队列中删除完成的挂单
                               selllist.remove(tran);
                           }
                           if (tran1.getUncompleted() == 0) {
                               //删除数据库中的挂单信息
                               tranService.removeById(tran1.getId());
                               //从队列中删除完成的挂单
                               trans.remove(tran1);
                           }
                           //完成交易将变量改为true
                           good = true;
                       } else {
                           //卖出数量大于买入数量

                           //卖出成交手数
                           tran1.setDealnum(tran.getUncompleted());
                           //卖出未成交手数
                           tran1.setUncompleted(tran1.getUncompleted() - tran1.getDealnum());
                           tranService.updateById(tran1);
                           //买入未成交手数
                           tran.setUncompleted(tran.getUncompleted() - tran1.getDealnum());
                           tranService.updateById(tran);
                           if (tran.getUncompleted() == 0) {
                               //删除数据库中的挂单信息
                               tranService.removeById(tran.getId());
                               //从队列中删除完成的挂单
                               selllist.remove(tran);
                           }
                           if (tran1.getUncompleted() == 0) {
                               //删除数据库中的挂单信息
                               tranService.removeById(tran1.getId());
                               //从队列中删除完成的挂单
                               trans.remove(tran1);
                           }
                           good = true;
                           
                       }
                       //判断是否完成交易
                       if (good) {
                           //修改持仓数量
                           LambdaQueryWrapper<Position> positionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                           positionLambdaQueryWrapper.eq(Position::getUserid, 2);
                           positionLambdaQueryWrapper.eq(Position::getTranname, tran.getName());
                           Position one = positionService.getOne(positionLambdaQueryWrapper);
                           if (one != null) {
                               //更新持仓数量
                               one.setNumber(one.getNumber() + tran1.getDealnum());
                               //更新成交均价
                               one.setSalary((one.getSalary() + tran.getSalary()) / 2);
                               //修改至数据库
                               positionService.updateById(one);
                           } else {
                               //没有持仓新建持仓对象
                               Position position = new Position();
                               //持仓时间
                               position.setTime(new Date());
                               //股票产品名称
                               position.setTranname(tran.getName());
                               //持仓数量
                               position.setNumber(tran.getDealnum());
                               //成交均价
                               position.setSalary(tran.getSalary());
                               positionService.save(position);
                           }
                           //添加买方成交记录
                           Report report = new Report();
                           //成交手数
                           report.setDealnum(tran1.getDealnum());
                           //成交股票产品名称
                           report.setTranname(tran.getName());
                           //委托价格
                           report.setSalary(tran.getSalary());
                           //买入方
                           report.setBuy(tran.getUsername());
                           //卖出方
                           report.setSell(tran1.getUsername());
                           //成交价格
                           report.setPrice(tran1.getSalary());
                           //委托单号
                           report.setTranId(tran.getId());
                           //状态
                           report.setStatus("订立");
                           //成交时间
                           report.setTime(new Date());
                           report.setAmount(report.getPrice() - report.getSalary());
                           //添加卖方成交记录
                           Report reportSell = new Report();
                           //成交手数
                           reportSell.setDealnum(tran1.getDealnum());
                           //成交股票产品名称
                           reportSell.setTranname(tran1.getName());
                           //委托价格
                           reportSell.setSalary(tran1.getSalary());
                           //买入方
                           reportSell.setBuy(tran.getUsername());
                           //卖出方
                           reportSell.setSell(tran1.getUsername());
                           //成交价格
                           reportSell.setPrice(tran.getSalary());
                           //委托单号
                           reportSell.setTranId(tran1.getId());
                           //状态
                           reportSell.setStatus("转让");
                           //成交时间
                           reportSell.setTime(new Date());
                           reportSell.setAmount(reportSell.getPrice() - reportSell.getSalary());
                           reportService.save(reportSell);
                           reportService.save(report);

                           //返还佣金
                           User byId = userService.getById(tran.getUserid());
                           User user = userService.getById(tran1.getUserid());
                           //定义佣金
                           Integer rebate = 0;
                           rebate = report.getDealnum() * byId.getRebate();
                           //买进卖出资金返回佣金佣金

                           Integer rebates = reportSell.getDealnum() * user.getRebate();
                       }
                   }
               }
            }
        }
        redisTemplate.boundValueOps("sellList").set(selllist);
    }
}
