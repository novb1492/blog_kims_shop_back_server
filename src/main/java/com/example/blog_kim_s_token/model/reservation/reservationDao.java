package com.example.blog_kim_s_token.model.reservation;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.example.blog_kim_s_token.model.payment.tryDeleteInter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;



public interface reservationDao extends JpaRepository<mainReservationDto,Integer> {
    
    @Query(value = "select  count(*) from reservation where r_date=? AND seat=?",nativeQuery = true)
    int findByRdate(Timestamp timestamp,String seat);

    @Query(value = "select  count(*) from reservation where date_and_time=? AND seat=?",nativeQuery = true)
    int findByTime(Timestamp timestamp,String seat);

    @Query(value = "select  * from reservation where email=? AND seat=?",nativeQuery = true)
    List<mainReservationDto>findByEmailNative(String email,String seat);

    @Query(value = "select a.*,b.price,c.*,v.*,k.*,(select count(*)from reservation where email=?)totalpage from reservation a inner join  product b on a.seat=b.product_name left join card c on a.payment_id=c.cmcht_trd_no left join vbank v on a.payment_id=v.vmcht_trd_no left join kakaopay k on a.payment_id=k.ktid where a.email=? order by a.id desc limit ?,?",nativeQuery = true)
    Optional<List<getClientInter>> findByEmailJoinOrderByIdDescNative(String email2,String email,int first,int pagesize);

    @Query(value = "select a.*,b.price,c.*,v.*,k.*,(select count(*)from reservation where email=? and r_date between ? and ?)totalpage from reservation a inner join  product b on a.seat=b.product_name left join card c on a.payment_id=c.cmcht_trd_no left join vbank v on a.payment_id=v.vmcht_trd_no left join kakaopay k on a.payment_id=k.ktid where a.email=? and a.r_date between ? and ? order by a.id desc limit ?,?",nativeQuery = true)
    Optional<List<getClientInter>>findByEmailJoinOrderByIdBetweenDescNative(String email2,Timestamp startDate2,Timestamp endDate2,String email,Timestamp startDate,Timestamp endDate,int first,int pagesize);

    @Query(value = "select count(*) from reservation where email=? and r_date between ? and ?",nativeQuery = true)
    int countByEmailNative(String email,Timestamp startDate,Timestamp endDate);

    List<mainReservationDto> findByPaymentId(String paymentId);

   @Query(value = "select a.*,b.price from reservation a inner join product b on a.seat=b.product_name where a.id=?",nativeQuery = true)
    Optional<reservationAndPriceInter> findByPaymentidJoinPriceNative(int id);

    @Query(value = "select a.*,b.price,c.*,v.*,k.* from reservation a inner join  product b on a.seat=b.product_name left join card c on a.payment_id=c.cmcht_trd_no left join vbank v on a.payment_id=v.vmcht_trd_no left join kakaopay k on a.payment_id=k.ktid where a.id=?",nativeQuery = true)
    Optional<getClientInter> findByIdJoinNative(int id);
 
    @Query(value = "select a.*,b.price  from reservation  a inner join product  b  on a.seat=b.product_name where a.id=? ",nativeQuery = true)
    Optional<tryDeleteInter> findBySeatJoin(int id);

    
    @Modifying
    @Transactional
    @Query(value = "update reservation r set r.payment_id=? where r.payment_id=?",nativeQuery = true)
    void updatepayment_idNative(String newpayment_id,String originpayment_id);



}
   
