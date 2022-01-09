package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.service.MemberService;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {
        // given

        Member member = Member.builder()
                .name("kim")
                .address(new Address("서울","서대문구","123-123"))
                .build();
        // when
        Long saveId = memberService.join(member);

        // then
        assertEquals(member, memberRepository.findOne(saveId));

    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        // given
        Member memberA = Member.builder()
                .name("kim")
                .address(new Address("서울","서대문구","123-123"))
                .build();
        Member memberB = Member.builder()
                .name("kim")
                .address(new Address("경기","서대문구","123-123"))
                .build();

        // when
        memberService.join(memberA);
        memberService.join(memberB); //예외가 발생해야 한다!!

        // then
//        assertThrows(IllegalStateException.class, () -> memberService.join(memberB));
        fail("예외가 발생해야 한다!!");
    }


}