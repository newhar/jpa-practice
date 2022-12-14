package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(value = false)
    public void save() {
        Member member = new Member();
        member.setName("jin");

        System.out.println("======= memberRepository.save start =======");
        Long savedId = memberRepository.save(member);
        System.out.println("======= memberRepository.save end ======= : " + String.valueOf(savedId));
        System.out.println("======= memberRepository.findOne start =======");
        Member foundMember = memberRepository.findOne(savedId);
        System.out.println("======= memberRepository.findOne end =======" + foundMember.toString());
        Assertions.assertThat(savedId).isEqualTo(foundMember.getId());
        Assertions.assertThat(member.getName()).isEqualTo(foundMember.getName());
        Assertions.assertThat(foundMember).isEqualTo(member); // jpa 동일성 보장
    }

    @Test
    public void find() {
    }
}