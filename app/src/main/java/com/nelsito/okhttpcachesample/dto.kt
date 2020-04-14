package com.nelsito.okhttpcachesample

data class GithubResponse(val id: String, val url: String, val owner: GithubRepoOwner)

data class GithubRepoOwner(val login: String)
