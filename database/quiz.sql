create index idx_coupon_id
    on member_coupon(coupon_id);

drop index idx_coupon_id
    on member_coupon;

create index idx_used_coupon
    on member_coupon(coupon_id, used);

--

show index from member_coupon;

explain select * from member_coupon where coupon_id = 1 limit 1;
