# blogJwtToken
# blogKim-s-Shop
# blog_kims_shop_back_server

kim's cafe업그레이드 버전입니다  
1.회원가입  
2.로그인/로그아웃  
3.마이페이지  
4.예약,일반 상품 시스템/결제시스템/재고정리시스템  
5.게시판 구현   
  
1.회원가입  
-카카오 주소서비스를 이용해 주소를 받습니다  
-coolsms로 휴대폰인증후 가입이 가능합니다  
시나리오  
-작성->휴대폰인증->요청시 인증db에 저장->인증번호 일치시 db에서 인증 플래그phonecheck=1로 변경->회원가입시도->유효성검증->이상없다면->완료  
2.로그인  
-네이버/카카오로그인가능  
-카카오 로그인 시 로그인 후 메세지 동의를 얻기위해 추가동의창 표시  
-jwt방식을 이용했습니다  
(처음으로 사용해서 많이 미흡합니다)  
-csrf역시 그냥 수기로 만든거여서 미흡합니다  
(아직 보안쪽 공부는 하지 못했습니다)  
시나리오  
-이메일/비번 검사->맞다면 security세션에 인증 주입->access/refresh/csrf토큰발급->refresh/csrf토큰 db저장->쿠키로 3개의 토큰 발급  
2.로그아웃  
시나리오  
-로그아웃 요청->refresh/csrf토큰 db에서 제거  
3.마이페이지  
-주소변경가능  
-비밀번호 변경가능  
-휴대폰번호변경가능  
시나리오  
-변경요청->인증번호요청->맞다면 db 플래그1 ->변경전 인증 db확인->1이라면변경  
  
여기까지 프로젝트 초반에 만들었습니다    
기억이 잘안나서 실제 작도원리는 조금다를 수도있습니다  
  
4.예약상품  
-카카오페이 별도연동  
-pg사 세틀뱅크 직접연동  
-연/월/일별 자리 조회가능  
예약 시나리오  
-자리선택->처음은 오늘 월로 조회->연/월에 따른 일별(이미 몇명이 차있나확인)/지난날짜인지 확인->그거따른 disabled 플래그 부여->시간선택->연/월/일에 따른 시간 조회(이미 몇명이 차있나확인)/지난날짜인지 확인 다찼다면 disable플래그 부여 및 해당시간 인원같이 표시  
  
결제 시나리오  
예약시도->검증(예약이가능한 연/월/일 가상계좌라면 예약자리 한시간전 까지만 가상계좌로 예약가능)->검증통과라면 임시 예약/결제 테이블 저장->request세틀뱅크->response세틀뱅크->상품에 따라 maindb로 이동/결제 정보 db저장->임시테이블 삭제  
  
5.재고정리시스템  
-스프링배치+스프링스케줄러를 이용해서 주기적으로 현재시간보다 가상계좌입금시간이 작으면 join해서 삭제되게 만들었습니다  
  
6.게시판  
-글쓰기  
-summernote 에디터  
-사진업로드시 aws s3 클라우드사용  
  
그밖의 기능  
-카카오메세지(나에게 보내기)  
(사업자 등록증이없어서 알림톡 사용불가능)  
원래 어떤 행위가 이뤄지면   
카카오메세지로 보내주려했지만 시간이없어서 연동하진 못한상태입니다  
  
이전 kim's cafe와 달라진점  
1.주소참조/변수참조 좀더 잘 이해하게됐습니다  
-결제 시스템을 만들면서 좀 더 잘이해 하게 된거 같습니다     
(주소참조=dto같은 경우 주소참조(주소 접근 해 값을 수정 유지가됨)/변수참조=함수를 벗어나면 유지되지 않고 return을 이용해 던져줘야한다(현재는 이렇게 이해중입니다))  
2.인터페이스 활용  
-비슷한 로직을 가진 경우 인터페이스를 사용했습니다  
3.인터페이스/추상클래스이해  
-인터페이스는 완성된 설계도이고/추상클래스는 뼈대가 같은 곳에서 출발하는 느낌이였습니다  
생각보다 많은부분을 인터페이스를 이용해서 최적화가 가능할거 같다라는 생각이 들었습니다  
4.sql 에 대해이해  
-join/limite등 sql에 대해 조금더 많은 이해가 가능했습니다  
5.좀더 명확해진 mvc  
-이전 컨트롤러에 비해 훨씬 서비스와 나눠서 정리했습니다  
6.폭넓어진 api사용  
-카카오/네이버/aws/세틀뱅크를 이용해 더 직접적이고 좋은 기능을 사용하였습니다  
7.transectional어노테이션에 대한이해  
-이전보다 조금더 이해가 높아져서 마구 붙히지 않고 있습니다 아직도 부족한거같습니다  
8.webConfig/cors정책/https대한이해  
-직접 자바 인증서도 만들어보고 cors문제도 해결하고 로컬 저장소 접근시 톰캣 설정도 하였습니다  
(사진저장시 로컬 저장소 구현했으나 aws s3이용중)  
  
  
아쉬운점  
-그럼에도 불구하고 인터페이스/추상클래스를 제대로 사용하고 있지 못한거 같습니다  
-보안 csrf/xss같은 보안적문제 이해가 너무 낮습니다  
-그 밖의 알고리즘 공부역시 더욱 필요하다고 생각합니다  
-만들다 보니 예외를 너무 많이 사용한거같습니다  
-어노테이션이해도가 아직도 낮은거같습니다     
-인터페이스 사용률이 너무 낮은거 같습니다    
-오타가 너무 많습니다 뒤늦게 발견해서 고치는 것도 위험하고 안좋은 버릇이여서 고쳐야 할거같습니다  
-스프링배치에대한 이해도가 아직 많이 낮습니다  
-db설계능력  
  
아 게시글 길이/댓글길이는...  
summernote를 좀더 알아봐야하는데   
국비지원 수업이 월요일 부터 spring레거시를 할거같아서  
목표가 수업이 진행되기 전에 이토이를 끝내야해서  
조금 아쉽게 마무리 되었습니다  
  
그밖의 자세한 이야기는  
https://cordingmonster.tistory.com/category/Spring%20boot%20kim%27s%20Shop 에 적어놓았습니다  
mysql   
card  
+--------------+--------------+------+-----+---------+----------------+
| Field        | Type         | Null | Key | Default | Extra          |
+--------------+--------------+------+-----+---------+----------------+
| cid          | int          | NO   | PRI | NULL    | auto_increment |
| cfn_nm       | varchar(255) | NO   |     | NULL    |                |
| cmcht_id     | varchar(255) | NO   |     | NULL    |                |
| cmcht_trd_no | varchar(255) | NO   |     | NULL    |                |
| cmethod      | varchar(255) | NO   |     | NULL    |                |
| c_created    | timestamp    | YES  |     | NULL    |                |
| ctrd_amt     | varchar(255) | NO   |     | NULL    |                |
| ctrd_no      | varchar(255) | NO   |     | NULL    |                |
| ccncl_ord    | int          | YES  |     | NULL    |                |
+--------------+--------------+------+-----+---------+----------------+
coment  
+-----------+--------------+------+-----+---------+----------------+
| Field     | Type         | Null | Key | Default | Extra          |
+-----------+--------------+------+-----+---------+----------------+
| cid       | int          | NO   | PRI | NULL    | auto_increment |
| c_created | datetime     | NO   |     | NULL    |                |
| cbid      | int          | NO   |     | NULL    |                |
| cemail    | varchar(255) | NO   |     | NULL    |                |
| coment    | varchar(300) | YES  |     | NULL    |                |
+-----------+--------------+------+-----+---------+----------------+
confrim  
+----------------+-------------+------+-----+---------+----------------+
| Field          | Type        | Null | Key | Default | Extra          |
+----------------+-------------+------+-----+---------+----------------+
| id             | int         | NO   | PRI | NULL    | auto_increment |
| created        | datetime    | YES  |     | NULL    |                |
| email          | varchar(30) | YES  |     | NULL    |                |
| emailcheck     | int         | YES  |     | 0       |                |
| emailtempnum   | varchar(6)  | YES  |     | NULL    |                |
| phonecheck     | int         | YES  |     | 0       |                |
| phone_num      | varchar(15) | YES  |     | NULL    |                |
| phone_temp_num | varchar(6)  | YES  |     | NULL    |                |
| requesttime    | int         | NO   |     | 1       |                |
+----------------+-------------+------+-----+---------+----------------+
csrftoken  
+------------+--------------+------+-----+---------+----------------+
| Field      | Type         | Null | Key | Default | Extra          |
+------------+--------------+------+-----+---------+----------------+
| id         | int          | NO   | PRI | NULL    | auto_increment |
| created    | datetime     | YES  |     | NULL    |                |
| csrf_token | varchar(255) | NO   |     | NULL    |                |
| user_id    | int          | NO   |     | NULL    |                |
| email      | varchar(255) | NO   |     | NULL    |                |
+------------+--------------+------+-----+---------+----------------+
food  
+-------------+--------------+------+-----+---------+----------------+
| Field       | Type         | Null | Key | Default | Extra          |
+-------------+--------------+------+-----+---------+----------------+
| fid         | int          | NO   | PRI | NULL    | auto_increment |
| femail      | varchar(255) | YES  |     | NULL    |                |
| fcreated    | datetime     | YES  |     | NULL    |                |
| f_count     | int          | NO   |     | NULL    |                |
| fname       | varchar(255) | NO   |     | NULL    |                |
| food_name   | varchar(255) | NO   |     | NULL    |                |
| fpayment_id | varchar(255) | NO   |     | NULL    |                |
+-------------+--------------+------+-----+---------+----------------+
jwtrefreshtoken  
+-----------+--------------+------+-----+---------+----------------+
| Field     | Type         | Null | Key | Default | Extra          |
+-----------+--------------+------+-----+---------+----------------+
| id        | int          | NO   | PRI | NULL    | auto_increment |
| created   | datetime     | YES  |     | NULL    |                |
| tokenname | varchar(255) | NO   |     | NULL    |                |
| userid    | int          | NO   |     | NULL    |                |
+-----------+--------------+------+-----+---------+----------------+
kakaologintoken  
+------------------------+--------------+------+-----+---------+----------------+
| Field                  | Type         | Null | Key | Default | Extra          |
+------------------------+--------------+------+-----+---------+----------------+
| id                     | int          | NO   | PRI | NULL    | auto_increment |
| access_token           | varchar(255) | NO   |     | NULL    |                |
| created                | datetime     | YES  |     | NULL    |                |
| email                  | varchar(255) | NO   |     | NULL    |                |
| refresh_token          | varchar(255) | NO   |     | NULL    |                |
| access_token_expiresin | datetime     | NO   |     | NULL    |                |
+------------------------+--------------+------+-----+---------+----------------+
kakaopay  
+-------------------+--------------+------+-----+---------+----------------+
| Field             | Type         | Null | Key | Default | Extra          |
+-------------------+--------------+------+-----+---------+----------------+
| kid               | int          | NO   | PRI | NULL    | auto_increment |
| kcid              | varchar(255) | NO   |     | NULL    |                |
| k_created         | timestamp    | YES  |     | NULL    |                |
| kpartner_order_id | varchar(255) | NO   |     | NULL    |                |
| kpartner_user_id  | varchar(255) | NO   |     | NULL    |                |
| ktax_free_amount  | varchar(255) | NO   |     | NULL    |                |
| ktid              | varchar(255) | NO   |     | NULL    |                |
| ktotal_amount     | int          | NO   |     | NULL    |                |
+-------------------+--------------+------+-----+---------+----------------+
paidproduct  
+--------------------+--------------+------+-----+---------+----------------+
| Field              | Type         | Null | Key | Default | Extra          |
+--------------------+--------------+------+-----+---------+----------------+
| id                 | int          | NO   | PRI | NULL    | auto_increment |
| created            | datetime     | YES  |     | NULL    |                |
| email              | varchar(255) | NO   |     | NULL    |                |
| kind               | varchar(255) | NO   |     | NULL    |                |
| name               | varchar(255) | NO   |     | NULL    |                |
| payment_id         | varchar(255) | NO   |     | NULL    |                |
| status             | varchar(255) | NO   |     | NULL    |                |
| total_price        | int          | NO   |     | NULL    |                |
| used_kind          | varchar(255) | NO   |     | NULL    |                |
| pay_method         | varchar(255) | NO   |     | NULL    |                |
| paidmcht_trd_no_id | varchar(255) | YES  | UNI | NULL    |                |
+--------------------+--------------+------+-----+---------+----------------+
reservation  
+---------------+--------------+------+-----+---------+----------------+
| Field         | Type         | Null | Key | Default | Extra          |
+---------------+--------------+------+-----+---------+----------------+
| id            | int          | NO   | PRI | NULL    | auto_increment |
| created       | datetime     | YES  |     | NULL    |                |
| date_and_time | datetime     | YES  |     | NULL    |                |
| email         | varchar(255) | NO   |     | NULL    |                |
| name          | varchar(255) | NO   |     | NULL    |                |
| payment_id    | varchar(255) | YES  |     | NULL    |                |
| seat          | varchar(255) | NO   |     | NULL    |                |
| time          | int          | YES  |     | NULL    |                |
| r_date        | datetime     | YES  |     | NULL    |                |
+---------------+--------------+------+-----+---------+----------------+
tempfood  
+--------------+--------------+------+-----+---------+----------------+
| Field        | Type         | Null | Key | Default | Extra          |
+--------------+--------------+------+-----+---------+----------------+
| tfid         | int          | NO   | PRI | NULL    | auto_increment |
| tfname       | varchar(255) | NO   |     | NULL    |                |
| tf_count     | int          | NO   |     | NULL    |                |
| tfcreated    | datetime     | YES  |     | NULL    |                |
| tfemail      | varchar(255) | NO   |     | NULL    |                |
| tfood_name   | varchar(255) | NO   |     | NULL    |                |
| tfpayment_id | varchar(255) | NO   |     | NULL    |                |
+--------------+--------------+------+-----+---------+----------------+
temporder  
+------------+--------------+------+-----+---------+----------------+
| Field      | Type         | Null | Key | Default | Extra          |
+------------+--------------+------+-----+---------+----------------+
| tpid       | int          | NO   | PRI | NULL    | auto_increment |
| tpaymentid | varchar(255) | NO   |     | NULL    |                |
| tpemail    | varchar(255) | NO   |     | NULL    |                |
| tpprice    | varchar(255) | NO   |     | NULL    |                |
| tpcreated  | datetime     | YES  |     | NULL    |                |
+------------+--------------+------+-----+---------+----------------+
tempreservation  
+------------------+--------------+------+-----+---------+----------------+
| Field            | Type         | Null | Key | Default | Extra          |
+------------------+--------------+------+-----+---------+----------------+
| trid             | int          | NO   | PRI | NULL    | auto_increment |
| tr_created       | datetime     | YES  |     | NULL    |                |
| tr_date_and_time | datetime     | YES  |     | NULL    |                |
| tr_email         | varchar(255) | NO   |     | NULL    |                |
| tr_name          | varchar(255) | NO   |     | NULL    |                |
| tr_paymentid     | varchar(255) | YES  |     | NULL    |                |
| tr_seat          | varchar(255) | NO   |     | NULL    |                |
| tr_time          | int          | YES  |     | NULL    |                |
| tr_status        | varchar(255) | NO   |     | NULL    |                |
| tr_rdate         | datetime     | YES  |     | NULL    |                |
+------------------+--------------+------+-----+---------+----------------+
user  
+-----------------+--------------+------+-----+---------+----------------+
| Field           | Type         | Null | Key | Default | Extra          |
+-----------------+--------------+------+-----+---------+----------------+
| id              | int          | NO   | PRI | NULL    | auto_increment |
| address         | varchar(40)  | NO   |     | NULL    |                |
| created         | datetime     | YES  |     | NULL    |                |
| detail_address  | varchar(20)  | NO   |     | NULL    |                |
| email           | varchar(30)  | NO   | UNI | NULL    |                |
| email_check     | int          | YES  |     | NULL    |                |
| extra_address   | varchar(50)  | YES  |     | NULL    |                |
| fail_login      | int          | YES  |     | NULL    |                |
| fail_login_time | int          | YES  |     | NULL    |                |
| name            | varchar(20)  | NO   |     | NULL    |                |
| phone_check     | int          | YES  |     | NULL    |                |
| phone_num       | varchar(15)  | NO   |     | NULL    |                |
| postcode        | varchar(10)  | NO   |     | NULL    |                |
| provider        | varchar(255) | YES  |     | NULL    |                |
| pwd             | varchar(200) | NO   |     | NULL    |                |
| role            | varchar(10)  | NO   |     | NULL    |                |
+-----------------+--------------+------+-----+---------+----------------+
vbank  
+--------------+--------------+------+-----+---------+----------------+
| Field        | Type         | Null | Key | Default | Extra          |
+--------------+--------------+------+-----+---------+----------------+
| vid          | int          | NO   | PRI | NULL    | auto_increment |
| vcreated     | timestamp    | YES  |     | NULL    |                |
| vexpire_dt   | datetime     | NO   |     | NULL    |                |
| vfn_cd       | varchar(255) | NO   |     | NULL    |                |
| vfn_nm       | varchar(255) | NO   |     | NULL    |                |
| vmcht_id     | varchar(255) | NO   |     | NULL    |                |
| vmcht_trd_no | varchar(255) | NO   |     | NULL    |                |
| vmethod      | varchar(255) | NO   |     | NULL    |                |
| vtl_acnt_no  | varchar(255) | NO   |     | NULL    |                |
| vtrd_amt     | varchar(255) | NO   |     | NULL    |                |
| vtrd_no      | varchar(255) | NO   |     | NULL    |                |
| vbankstatus  | varchar(255) | NO   |     | NULL    |                |
| vtrd_dtm     | datetime     | YES  |     | NULL    |                |
| vcncl_ord    | int          | YES  |     | NULL    |                |
+--------------+--------------+------+-----+---------+----------------+