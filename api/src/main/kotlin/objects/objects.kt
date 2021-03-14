package ai.alice.api.objects

import ai.alice.api.objects.provider.Provider
import ai.alice.api.objects.provider.ProviderFactory

interface ObjectFactory : ProviderFactory, ContainerFactory {

  fun <T : Any> convert(`object`: Any, type: Class<T>): Provider<T>
}
