package com.arij.ajir.domain.member.model

import com.arij.ajir.common.exception.ModelNotSavedException
import com.arij.ajir.domain.member.dto.MemberResponse
import com.arij.ajir.domain.team.model.Team
import jakarta.persistence.*
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.PostgreSQLEnumJdbcType


@Entity
class Member (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType::class)
    var role: Role = Role.USER,

    @Column(nullable = false, unique = true, length = 100)
    var email: String? = null,

    @Column(nullable = false, length = 64, name = "pw1")
    var password: String? = null,

    @Column(nullable = false, length = 64, name = "pw2")
    var password2: String? = null,

    @Column(nullable = false, length = 64, name = "pw3")
    var password3: String? = null,

    @Column(nullable = false, unique = true, length = 20)
    var nickname: String? = null,
) {
    fun toResponse(): MemberResponse {
        return MemberResponse(
            memberId = this.id ?: throw ModelNotSavedException("Member"),
            teamName = this.team!!.name,
            email = this.email!!,
            nickname = this.nickname!!,
            role = role.name
        )
    }

    fun giveTeam(team: Team){
        this.team = team
        this.role = Role.LEADER
    }

    fun fireTeam(team: Team){
        this.role = Role.USER
    }
}