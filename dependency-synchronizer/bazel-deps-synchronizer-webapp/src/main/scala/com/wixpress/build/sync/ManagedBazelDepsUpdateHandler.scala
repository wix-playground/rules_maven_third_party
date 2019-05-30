package com.wix.build.sync

import com.wix.build.sync.api.{BazelManagedDepsSyncEnded, ThirdPartyArtifact}
import com.wix.build.bazel.BazelDependenciesReader
import com.wix.ci.greyhound.events.VcsUpdate
import com.wix.greyhound.DetailedProduceResult
import com.wix.greyhound.producer.builder.GreyhoundResilientProducer
import com.wix.vi.githubtools.masterguard.enforceadmins.MasterEnforcer

import scala.concurrent.Future

class ManagedBazelDepsUpdateHandler(gitSettings: GitSettings, masterEnforcer: MasterEnforcer, syncEndedProducer: GreyhoundResilientProducer) {
  def publishEventWithManagedBzlDeps(message: VcsUpdate): Future[DetailedProduceResult] = {
    val thirdPartyManagedArtifacts = readManagedArtifacts()
    //consumed by LabelDex!
    syncEndedProducer.produce(BazelManagedDepsSyncEnded(thirdPartyManagedArtifacts))
  }

  private def readManagedArtifacts(): Set[ThirdPartyArtifact] = {
    val managedDepsBazelRepository = ManagedBazelDepsClone.localCloneOfManagedDepsBazelRepository(gitSettings, masterEnforcer)
    val managedDepsRepoReader = new BazelDependenciesReader(managedDepsBazelRepository.resetAndCheckoutMaster())
    managedDepsRepoReader.allDependenciesAsMavenDependencyNodes()
      .map(d => ThirdPartyArtifact(d.baseDependency.coordinates.groupId,
        d.baseDependency.coordinates.artifactId,
        d.baseDependency.coordinates.version,
        d.baseDependency.coordinates.packaging.value,
        d.baseDependency.coordinates.classifier,
        None))
  }
}