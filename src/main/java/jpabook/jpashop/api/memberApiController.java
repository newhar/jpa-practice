package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class memberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();

        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse join( @RequestParam CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long memberId = memberService.join(member);

        return new CreateMemberResponse(memberId);
    }

    @PostMapping("/api/v2/members/{id}")
    public UpdateMemberResponse update(@PathVariable(value = "id") Long id,
                                 @Valid @RequestParam UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static public class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
}
