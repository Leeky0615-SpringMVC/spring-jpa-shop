package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 데이터를 변경하는 것은 Transactional이 꼭 있어야한다.
 * readOnly=true 옵션
 * JPA가 조회하는 곳에서는 성능을 조금더 최적화한다
 * 더티 체킹 x, 읽기전용이라고 하면 리소스를 많이 사용X 하기도하는 이점이 있다.
 * 전체적으로는 true로 놔두고 수정이 있는 부분에만 따로 다시 걸어준다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) throw new IllegalStateException("이미 존재하는 회원입니다.");
    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long id) {
        return memberRepository.findOne(id);
    }

    //회원 정보 수정
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.updateName(name);
    }

}

