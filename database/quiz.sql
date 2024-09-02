create index idx_coupon_id
    on member_coupon(coupon_id);

drop index idx_coupon_id
    on member_coupon;

create index idx_used_coupon
    on member_coupon(coupon_id, used);

create index idx_monthly_member_benefit_amount
    on monthly_member_benefit(year, month, coupon_discount_amount);

--

show index from member_coupon;

explain select * from member_coupon where coupon_id = 1 limit 1;
