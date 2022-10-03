package ua.POE.Task_abon.domain.usecases

import ua.POE.Task_abon.domain.repository.TaskRepository

class GetTaskInfoListUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke() = repository.getTaskInfoList()
}