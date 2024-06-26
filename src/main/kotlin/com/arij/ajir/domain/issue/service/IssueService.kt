package com.arij.ajir.domain.issue.service

import com.arij.ajir.common.exception.ModelNotFoundException
import com.arij.ajir.domain.issue.dto.*
import com.arij.ajir.domain.issue.model.Issue
import com.arij.ajir.domain.issue.repository.IssueRepository
import com.arij.ajir.domain.member.model.Member
import com.arij.ajir.domain.member.model.Role
import com.arij.ajir.domain.member.repository.MemberRepository
import com.arij.ajir.infra.security.UserPrincipal
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IssueService(
    private val issueRepository: IssueRepository,
    private val memberRepository: MemberRepository,
) {

    fun getIssueById(issueId: Long, email: String): IssueResponseWithCommentResponse {

        val issue = checkAuthority(issueId, email)

        return issue.toResponseWithCommentResponse()
    }

    fun createIssue(request: IssueCreateRequest, email: String): IssueIdResponse {
        val member: Member = memberRepository.findByEmail(email) ?: throw ModelNotFoundException("Member", email)

        if (member.role.name != Role.ADMIN.name) {
            if (member.team!!.name == "DUMMY") {
                throw IllegalStateException("Dummy team Can't CRUD")
            }
        }

        val issue = Issue(
            title = request.title,
            content = request.content,
            priority = request.priority,
            workingStatus = request.workingStatus,
            member = member,
            team = member.team!!
        )

        return issueRepository.save(issue).toIdResponse()
    }

    fun updateIssue(issueId: Long, request: IssueUpdateRequest, email: String) {

        val issue = checkAuthority(issueId, email)

        issue.updateTitleAndContent(request)

        issueRepository.save(issue)
    }

    fun updatePriority(issueId: Long, request: PriorityUpdateRequest, email: String) {

        val issue = checkAuthority(issueId, email)

        issue.updatePriority(request.priority)
    }

    fun updateWorkingStatus(issueId: Long, request: WorkingStatusUpdateRequest, email: String) {

        val issue = checkAuthority(issueId, email)

        issue.updateWorkingStatus(request.workingStatus)
    }

    fun deleteIssue(issueId: Long, email: String) {

        val issue = checkAuthority(issueId, email)

        issue.delete()
        issueRepository.save(issue)
    }

    private fun checkAuthority(issueId: Long, email: String): Issue {

        val member: Member = memberRepository.findByEmail(email) ?: throw ModelNotFoundException("Member", email)
        val issue = issueRepository.findIssueByIdAndDeletedIsFalse(issueId)
            .orElseThrow() { IllegalStateException("Issue not found") }

        if (member.role.name != Role.ADMIN.name) {
            if (member.team!!.name == "DUMMY") {
                throw IllegalStateException("Dummy team Can't CRUD")
            }
            if (issue.team != member.team) {
                throw IllegalStateException("team not same")
            }
        }

        return issue
    }

    fun searchIssues(
        topic: String?,
        keyword: String?,
        orderBy: String,
        ascend: Boolean,
        principal: UserPrincipal
    ): List<IssueResponse> {
        val member =
            memberRepository.findByIdOrNull(principal.id) ?: throw ModelNotFoundException("멤버", principal.id.toString())

        val teamId: Long = if (member.role.name != Role.ADMIN.name) member.team?.id!! else -1

        return issueRepository.searchIssues(topic, keyword, orderBy, ascend, teamId).map { it.toResponse() }
    }
}