package ua.POE.Task_abon.domain.usecases

import ua.POE.Task_abon.domain.repository.TaskRepository

class GetTaskInfoUseCase(val repository: TaskRepository) {

   // operator fun invoke(taskId: String) = repository.getTaskInfo(taskId)
}